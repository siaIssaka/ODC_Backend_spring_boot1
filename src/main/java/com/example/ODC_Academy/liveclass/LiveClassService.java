package com.example.ODC_Academy.liveclass;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.ODC_Academy.liveclass.LiveClassDtos.*;

@Service
@Transactional
public class LiveClassService {
    private final LiveClassRepository sessions;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public LiveClassService(LiveClassRepository sessions, CourseRepository courses, EnrollmentRepository enrollments) {
        this.sessions = sessions;
        this.courses = courses;
        this.enrollments = enrollments;
    }

    public SessionDto schedule(Long courseId, CreateRequest request, User me) {
        Course course = course(courseId);
        assertTrainerOwner(me, course);
        LiveClassSession session = sessions.save(LiveClassSession.builder()
                .course(course)
                .createdBy(me)
                .title(request.title().trim())
                .roomName("odc-" + UUID.randomUUID())
                .scheduledAt(request.scheduledAt())
                .status(LiveSessionStatus.PLANIFIE)
                .build());
        return toDto(session, me);
    }

    @Transactional(readOnly = true)
    public List<SessionDto> forCourse(Long courseId, User me) {
        Course course = course(courseId);
        if (me.getRole() == Role.APPRENANT) {
            boolean activeEnrollment = enrollments.findByUserIdAndCourseId(me.getId(), courseId)
                    .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE).orElse(false);
            if (!activeEnrollment) {
                throw new AccessDeniedException("Inscrivez-vous au cours pour consulter ses séances");
            }
        } else if (me.getRole() == Role.FORMATEUR && !CourseAccess.edits(me, course)) {
            throw new AccessDeniedException("Seul le formateur créateur peut gérer les séances de ce cours");
        }
        return sessions.findByCourseIdOrderByScheduledAtAsc(courseId).stream().map(s -> toDto(s, me)).toList();
    }

    public SessionDto start(Long sessionId, User me) {
        LiveClassSession session = find(sessionId);
        assertTrainerOwner(me, session.getCourse());
        if (session.getStatus() != LiveSessionStatus.PLANIFIE) {
            throw new com.example.ODC_Academy.exception.BadRequestException("Cette séance n'est plus planifiée");
        }
        session.setStatus(LiveSessionStatus.PREPARATION);
        session.setStartedAt(LocalDateTime.now());
        return toDto(session, me);
    }

    public SessionDto publish(Long sessionId, User me) {
        LiveClassSession session = find(sessionId);
        assertTrainerOwner(me, session.getCourse());
        if (session.getStatus() != LiveSessionStatus.PREPARATION) {
            throw new com.example.ODC_Academy.exception.BadRequestException("La séance n'est pas en préparation");
        }
        session.setStatus(LiveSessionStatus.EN_COURS);
        return toDto(session, me);
    }

    public SessionDto end(Long sessionId, User me) {
        LiveClassSession session = find(sessionId);
        assertTrainerOwner(me, session.getCourse());
        if (session.getStatus() != LiveSessionStatus.EN_COURS
                && session.getStatus() != LiveSessionStatus.PREPARATION) {
            throw new com.example.ODC_Academy.exception.BadRequestException("Cette séance n'est pas en cours");
        }
        session.setStatus(LiveSessionStatus.TERMINE);
        session.setEndedAt(LocalDateTime.now());
        return toDto(session, me);
    }

    private void assertTrainerOwner(User me, Course course) {
        if (!CourseAccess.edits(me, course)) {
            throw new AccessDeniedException("Seul le formateur créateur peut gérer les séances de ce cours");
        }
    }

    private Course course(Long courseId) {
        return courses.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
    }

    private LiveClassSession find(Long id) {
        return sessions.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Séance", id));
    }

    private SessionDto toDto(LiveClassSession session, User viewer) {
        boolean preparingAndOwner = session.getStatus() == LiveSessionStatus.PREPARATION
                && session.getCreatedBy().getId().equals(viewer.getId());
        String roomName = session.getStatus() == LiveSessionStatus.EN_COURS || preparingAndOwner
                ? session.getRoomName() : null;
        return new SessionDto(session.getId(), session.getCourse().getId(), session.getTitle(), roomName,
                session.getScheduledAt(), session.getStatus(), session.getStartedAt(), session.getEndedAt());
    }
}
