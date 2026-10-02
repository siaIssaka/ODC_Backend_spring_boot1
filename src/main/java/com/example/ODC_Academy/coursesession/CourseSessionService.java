package com.example.ODC_Academy.coursesession;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.FormationRepository;
import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentBatchResult;
import com.example.ODC_Academy.enrollment.EnrollmentCandidatesDTO;
import com.example.ODC_Academy.enrollment.EnrollmentLearnerDTO;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserDTO;
import com.example.ODC_Academy.user.UserMapper;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseSessionService {
    private final CourseSessionRepository sessions;
    private final CourseRepository courses;
    private final FormationRepository formations;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;

    public CourseSessionService(CourseSessionRepository sessions, CourseRepository courses, FormationRepository formations,
                                EnrollmentRepository enrollments, UserRepository users) {
        this.sessions = sessions;
        this.courses = courses;
        this.formations = formations;
        this.enrollments = enrollments;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<CourseSessionDTO> getFormationSessions(Long formationId) {
        formations.findById(formationId).orElseThrow(() -> ResourceNotFoundException.of("Formation", formationId));
        return sessions.findByFormationIdOrderByStartsAtAsc(formationId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCohortOverviewDTO> getAdminOverview() {
        return sessions.findAll().stream()
                .sorted(Comparator.comparing(CourseSession::getStartsAt).reversed())
                .map(session -> {
                    Map<Long, List<Enrollment>> enrollmentsByLearner = enrollments
                            .findByCourseSession_Id(session.getId()).stream()
                            .collect(Collectors.groupingBy(enrollment -> enrollment.getUser().getId()));
                    List<EnrollmentLearnerDTO> enrolledLearners = enrollmentsByLearner.values().stream()
                            .sorted(Comparator.comparing(items -> items.getFirst().getUser().getNom()))
                            .map(items -> new EnrollmentLearnerDTO(
                                    UserMapper.toDto(items.getFirst().getUser()),
                                    items.stream().map(item -> item.getCourse().getId()).distinct().toList(),
                                    items.stream().map(Enrollment::getStatus).findFirst().orElse(EnrollmentStatus.ACTIVE),
                                    items.stream().map(Enrollment::getEnrolledAt).min(Comparator.naturalOrder()).orElse(null)))
                            .toList();
                    List<CourseSessionCourseDTO> sessionCourses = session.getCourses().stream()
                            .sorted(Comparator.comparing(Course::getTitle))
                            .map(course -> new CourseSessionCourseDTO(course.getId(), course.getTitle()))
                            .toList();
                    return new AdminCohortOverviewDTO(
                            session.getId(),
                            session.getFormation() == null ? null : session.getFormation().getTitle(),
                            session.getName(),
                            session.getStartsAt(),
                            session.getEndsAt(),
                            sessionCourses,
                            enrolledLearners);
                })
                .toList();
    }

    @Transactional
    public CourseSessionDTO create(Long formationId, CreateCourseSessionRequest request) {
        Formation formation = formations.findById(formationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Formation", formationId));
        Set<Course> sessionCourses = getActiveCoursesForFormation(formation, request.courseIds());
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new BadRequestException("La date de fin doit être postérieure à la date de début.");
        }
        CourseSession session = sessions.save(CourseSession.builder()
                .formation(formation)
                .courses(sessionCourses)
                .name(request.name().trim())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .build());
        return toDto(session);
    }

    @Transactional
    public CourseSessionDTO addCourses(Long sessionId, AddCoursesToSessionRequest request) {
        CourseSession session = getSession(sessionId);
        if (session.getFormation() == null) {
            throw new BadRequestException("Cette ancienne session n'est rattachée à aucune formation.");
        }
        session.getCourses().addAll(getActiveCoursesForFormation(session.getFormation(), request.courseIds()));
        return toDto(sessions.save(session));
    }

    @Transactional(readOnly = true)
    public EnrollmentCandidatesDTO getCandidates(Long sessionId) {
        CourseSession session = getSession(sessionId);
        List<Enrollment> enrolled = enrollments.findByCourseSession_Id(sessionId);
        Set<Long> cohortCourseIds = session.getCourses().stream().map(Course::getId).collect(Collectors.toSet());
        Map<Long, List<Enrollment>> byLearner = enrolled.stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getUser().getId()));
        List<UserDTO> candidates = users.findByRoleAndActiveTrue(Role.APPRENANT).stream()
                .filter(user -> cohortCourseIds.isEmpty()
                        || !byLearner.getOrDefault(user.getId(), List.of()).stream()
                        .map(enrollment -> enrollment.getCourse().getId()).collect(Collectors.toSet())
                        .containsAll(cohortCourseIds))
                .sorted(Comparator.comparing(User::getNom).thenComparing(User::getPrenom))
                .map(UserMapper::toDto)
                .toList();
        List<EnrollmentLearnerDTO> enrolledLearners = byLearner.values().stream()
                .sorted(Comparator.comparing(items -> items.getFirst().getUser().getNom()))
                .map(items -> new EnrollmentLearnerDTO(
                        UserMapper.toDto(items.getFirst().getUser()),
                        items.stream().map(item -> item.getCourse().getId()).distinct().toList(),
                        items.getFirst().getStatus(),
                        items.stream().map(Enrollment::getEnrolledAt).min(Comparator.naturalOrder()).orElse(null)))
                .toList();
        return new EnrollmentCandidatesDTO(enrolledLearners.size(), candidates, enrolledLearners);
    }

    @Transactional
    public EnrollmentBatchResult enroll(Long sessionId, List<Long> userIds) {
        CourseSession session = getSession(sessionId);
        if (session.getCourses().isEmpty()) {
            throw new BadRequestException("Ajoutez au moins un cours à cette cohorte avant d'inscrire des apprenants.");
        }
        if (session.getCourses().stream().anyMatch(course -> !course.isActive())) {
            throw new BadRequestException("Tous les cours de cette cohorte doivent être actifs pour inscrire des apprenants.");
        }
        if (new HashSet<>(userIds).size() != userIds.size()) {
            throw new BadRequestException("La liste contient des apprenants en double.");
        }
        List<User> learners = users.findAllById(userIds);
        if (learners.size() != userIds.size()) {
            throw new BadRequestException("Un ou plusieurs apprenants sélectionnés n'existent plus.");
        }
        if (learners.stream().anyMatch(user -> user.getRole() != Role.APPRENANT || !user.isActive())) {
            throw new BadRequestException("Seuls les apprenants actifs peuvent être inscrits.");
        }

        Map<Long, Set<Long>> alreadyEnrolled = enrollments.findByCourseSession_Id(sessionId).stream()
                .collect(Collectors.groupingBy(
                        enrollment -> enrollment.getUser().getId(),
                        Collectors.mapping(enrollment -> enrollment.getCourse().getId(), Collectors.toSet())));
        Set<Long> courseIds = session.getCourses().stream().map(Course::getId).collect(Collectors.toSet());
        List<Enrollment> newEnrollments = learners.stream()
                .flatMap(user -> session.getCourses().stream()
                        .filter(course -> !alreadyEnrolled.getOrDefault(user.getId(), Set.of()).contains(course.getId()))
                        .map(course -> Enrollment.builder()
                                .user(user)
                                .course(course)
                                .courseSession(session)
                                .status(EnrollmentStatus.ACTIVE)
                                .build()))
                .toList();
        enrollments.saveAll(newEnrollments);
        long alreadyFullyEnrolledCount = learners.stream()
                .filter(user -> alreadyEnrolled.getOrDefault(user.getId(), Set.of()).containsAll(courseIds))
                .count();
        long newlyEnrolledCount = learners.size() - alreadyFullyEnrolledCount;
        return new EnrollmentBatchResult(Math.toIntExact(newlyEnrolledCount), Math.toIntExact(alreadyFullyEnrolledCount));
    }

    private Set<Course> getActiveCoursesForFormation(Formation formation, List<Long> courseIds) {
        if (new HashSet<>(courseIds).size() != courseIds.size()) {
            throw new BadRequestException("La liste contient des cours en double.");
        }
        List<Course> selectedCourses = courses.findAllById(courseIds);
        if (selectedCourses.size() != courseIds.size()) {
            throw new BadRequestException("Un ou plusieurs cours sélectionnés n'existent plus.");
        }
        if (selectedCourses.stream().anyMatch(course -> !course.isActive()
                || course.getFormation() == null
                || !course.getFormation().getId().equals(formation.getId()))) {
            throw new BadRequestException("Tous les cours doivent être actifs et appartenir à la formation choisie.");
        }
        return new HashSet<>(selectedCourses);
    }

    private CourseSession getSession(Long sessionId) {
        return sessions.findById(sessionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Session de cours", sessionId));
    }

    private CourseSessionDTO toDto(CourseSession session) {
        int enrolledCount = Math.toIntExact(enrollments.countByCourseSession_Id(session.getId()));
        List<CourseSessionCourseDTO> sessionCourses = session.getCourses().stream()
                .sorted(Comparator.comparing(Course::getTitle))
                .map(course -> new CourseSessionCourseDTO(course.getId(), course.getTitle()))
                .toList();
        Formation formation = session.getFormation();
        return new CourseSessionDTO(session.getId(),
                formation == null ? null : formation.getId(),
                formation == null ? null : formation.getTitle(),
                session.getName(), session.getStartsAt(), session.getEndsAt(), sessionCourses, enrolledCount);
    }
}
