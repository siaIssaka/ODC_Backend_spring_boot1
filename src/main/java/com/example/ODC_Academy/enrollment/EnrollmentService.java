package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.exception.DuplicateResourceException;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserDTO;
import com.example.ODC_Academy.user.UserMapper;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                              UserRepository userRepository,
                              CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public List<Enrollment> getEnrollmentsByUser(Long userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public EnrollmentCandidatesDTO getCourseCandidates(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        List<Enrollment> courseEnrollments = enrollmentRepository.findByCourseId(courseId);
        Set<Long> enrolledUserIds = courseEnrollments.stream()
                .map(enrollment -> enrollment.getUser().getId())
                .collect(Collectors.toSet());
        List<UserDTO> learners = userRepository.findByRoleAndActiveTrue(Role.APPRENANT).stream()
                .sorted(java.util.Comparator.comparing(User::getNom).thenComparing(User::getPrenom))
                .filter(user -> !enrolledUserIds.contains(user.getId()))
                .map(UserMapper::toDto)
                .toList();
        List<EnrollmentLearnerDTO> enrolledLearners = courseEnrollments.stream()
                .sorted(java.util.Comparator.comparing(enrollment -> enrollment.getUser().getNom()))
                .map(enrollment -> new EnrollmentLearnerDTO(
                        UserMapper.toDto(enrollment.getUser()),
                        List.of(enrollment.getCourse().getId()),
                        enrollment.getStatus(),
                        enrollment.getEnrolledAt()))
                .toList();
        return new EnrollmentCandidatesDTO(courseEnrollments.size(), learners, enrolledLearners);
    }

    @Transactional
    public EnrollmentBatchResult enrollUsersToCourse(Long courseId, List<Long> userIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        if (!course.isActive()) {
            throw new BadRequestException("Ce cours n'est pas ouvert aux inscriptions");
        }
        if (new HashSet<>(userIds).size() != userIds.size()) {
            throw new BadRequestException("La liste contient des apprenants en double.");
        }

        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new BadRequestException("Un ou plusieurs apprenants sélectionnés n'existent plus.");
        }
        if (users.stream().anyMatch(user -> user.getRole() != Role.APPRENANT || !user.isActive())) {
            throw new BadRequestException("Seuls les apprenants actifs peuvent être inscrits.");
        }

        Set<Long> alreadyEnrolledIds = enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollment -> enrollment.getUser().getId())
                .collect(Collectors.toSet());
        List<Enrollment> newEnrollments = users.stream()
                .filter(user -> !alreadyEnrolledIds.contains(user.getId()))
                .map(user -> Enrollment.builder()
                        .user(user)
                        .course(course)
                        .status(EnrollmentStatus.ACTIVE)
                        .build())
                .toList();
        enrollmentRepository.saveAll(newEnrollments);
        return new EnrollmentBatchResult(newEnrollments.size(), users.size() - newEnrollments.size());
    }

    public Enrollment enrollUserToCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));

        if (user.getRole() != Role.APPRENANT) {
            throw new AccessDeniedException("Seuls les apprenants peuvent s'inscrire à un cours");
        }
        if (!course.isActive()) {
            throw new com.example.ODC_Academy.exception.BadRequestException("Ce cours n'est pas ouvert aux inscriptions");
        }
        if (enrollmentRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            throw new DuplicateResourceException("L'utilisateur est déjà inscrit à ce cours");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        return enrollmentRepository.save(enrollment);
    }
}
