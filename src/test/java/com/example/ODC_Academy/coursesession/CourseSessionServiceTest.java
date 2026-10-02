package com.example.ODC_Academy.coursesession;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.FormationRepository;
import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseSessionServiceTest {
    @Mock private CourseSessionRepository sessions;
    @Mock private CourseRepository courses;
    @Mock private FormationRepository formations;
    @Mock private EnrollmentRepository enrollments;
    @Mock private UserRepository users;
    @InjectMocks private CourseSessionService service;

    @Test
    void createsSessionWhenEndIsAfterStart() {
        Formation formation = Formation.builder().id(6L).title("Développement").build();
        Course course = Course.builder().id(8L).title("Java").active(true).formation(formation).build();
        Course secondCourse = Course.builder().id(9L).title("Spring").active(true).formation(formation).build();
        LocalDateTime startsAt = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime endsAt = startsAt.plusDays(30);
        when(formations.findById(6L)).thenReturn(Optional.of(formation));
        when(courses.findAllById(List.of(8L, 9L))).thenReturn(List.of(course, secondCourse));
        when(sessions.save(any(CourseSession.class))).thenAnswer(invocation -> {
            CourseSession session = invocation.getArgument(0);
            session.setId(11L);
            return session;
        });

        CourseSessionDTO created = service.create(6L,
                new CreateCourseSessionRequest("Cohorte octobre", startsAt, endsAt, List.of(8L, 9L)));

        assertEquals(11L, created.id());
        assertEquals(6L, created.formationId());
        assertEquals(List.of(8L, 9L), created.courses().stream().map(CourseSessionCourseDTO::id).toList());
        assertEquals(startsAt, created.startsAt());
    }

    @Test
    void rejectsSessionWithEndBeforeStart() {
        Formation formation = Formation.builder().id(6L).build();
        Course course = Course.builder().id(8L).active(true).formation(formation).build();
        when(formations.findById(6L)).thenReturn(Optional.of(formation));
        when(courses.findAllById(List.of(8L))).thenReturn(List.of(course));
        LocalDateTime startsAt = LocalDateTime.of(2026, 10, 10, 9, 0);

        assertThrows(BadRequestException.class, () -> service.create(6L,
                new CreateCourseSessionRequest("Cohorte invalide", startsAt, startsAt.minusDays(1), List.of(8L))));
    }

    @Test
    void allowsLearnerToEnrollInTwoSessionsOfTheSameCourse() {
        User learner = User.builder().id(3L).role(Role.APPRENANT).active(true).build();
        Course course = Course.builder().id(8L).active(true).build();
        CourseSession first = CourseSession.builder().id(11L).courses(Set.of(course)).build();
        CourseSession second = CourseSession.builder().id(12L).courses(Set.of(course)).build();
        when(sessions.findById(11L)).thenReturn(Optional.of(first));
        when(sessions.findById(12L)).thenReturn(Optional.of(second));
        when(users.findAllById(List.of(3L))).thenReturn(List.of(learner));
        when(enrollments.findByCourseSession_Id(11L)).thenReturn(List.of());
        when(enrollments.findByCourseSession_Id(12L)).thenReturn(List.of());

        assertEquals(1, service.enroll(11L, List.of(3L)).enrolledCount());
        assertEquals(1, service.enroll(12L, List.of(3L)).enrolledCount());

        verify(enrollments).saveAll(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.iterator().next().getCourseSession() == first));
        verify(enrollments).saveAll(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.iterator().next().getCourseSession() == second));
    }

    @Test
    void adminOverviewListsCohortCoursesAndGroupsLearnerEnrollments() {
        Formation formation = Formation.builder().id(6L).title("Développement").build();
        Course java = Course.builder().id(8L).title("Java").build();
        Course spring = Course.builder().id(9L).title("Spring").build();
        LocalDateTime startsAt = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime enrolledAt = startsAt.plusDays(1);
        User learner = User.builder().id(3L).nom("Dupont").prenom("Jean").email("jean@test.com")
                .role(Role.APPRENANT).active(true).build();
        CourseSession cohort = CourseSession.builder().id(11L).formation(formation).name("Octobre")
                .startsAt(startsAt).endsAt(startsAt.plusDays(30)).courses(Set.of(java, spring)).build();
        when(sessions.findAll()).thenReturn(List.of(cohort));
        when(enrollments.findByCourseSession_Id(11L)).thenReturn(List.of(
                Enrollment.builder().user(learner).course(java).status(EnrollmentStatus.ACTIVE).enrolledAt(enrolledAt).build(),
                Enrollment.builder().user(learner).course(spring).status(EnrollmentStatus.ACTIVE).enrolledAt(enrolledAt).build()));

        List<AdminCohortOverviewDTO> overview = service.getAdminOverview();

        assertEquals(1, overview.size());
        assertEquals("Développement", overview.getFirst().formationTitle());
        assertEquals(List.of(8L, 9L), overview.getFirst().courses().stream().map(CourseSessionCourseDTO::id).sorted().toList());
        assertEquals(1, overview.getFirst().enrolledLearners().size());
        assertEquals(List.of(8L, 9L), overview.getFirst().enrolledLearners().getFirst().courseIds().stream().sorted().toList());
    }
}
