package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.DuplicateResourceException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @Mock private EnrollmentRepository enrollments;
    @Mock private UserRepository users;
    @Mock private CourseRepository courses;
    @InjectMocks private EnrollmentService service;

    @Test
    void enrollsAnApprenticeInAnActiveCourse() {
        User learner = User.builder().id(3L).role(Role.APPRENANT).build();
        Course course = Course.builder().id(8L).active(true).build();
        when(users.findById(3L)).thenReturn(Optional.of(learner));
        when(courses.findById(8L)).thenReturn(Optional.of(course));
        when(enrollments.findByUserIdAndCourseId(3L, 8L)).thenReturn(Optional.empty());
        when(enrollments.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment created = service.enrollUserToCourse(3L, 8L);

        assertEquals(learner, created.getUser());
        assertEquals(course, created.getCourse());
        verify(enrollments).save(any(Enrollment.class));
    }

    @Test
    void rejectsNonLearners() {
        User trainer = User.builder().id(3L).role(Role.FORMATEUR).build();
        when(users.findById(3L)).thenReturn(Optional.of(trainer));
        when(courses.findById(8L)).thenReturn(Optional.of(Course.builder().id(8L).active(true).build()));

        assertThrows(AccessDeniedException.class, () -> service.enrollUserToCourse(3L, 8L));
        verify(enrollments, never()).save(any(Enrollment.class));
    }

    @Test
    void rejectsInactiveCoursesAndDuplicateEnrollments() {
        User learner = User.builder().id(3L).role(Role.APPRENANT).build();
        when(users.findById(3L)).thenReturn(Optional.of(learner));
        when(courses.findById(8L)).thenReturn(Optional.of(Course.builder().id(8L).active(false).build()));
        assertThrows(BadRequestException.class, () -> service.enrollUserToCourse(3L, 8L));

        when(courses.findById(8L)).thenReturn(Optional.of(Course.builder().id(8L).active(true).build()));
        when(enrollments.findByUserIdAndCourseId(3L, 8L)).thenReturn(Optional.of(new Enrollment()));
        assertThrows(DuplicateResourceException.class, () -> service.enrollUserToCourse(3L, 8L));
        verify(enrollments, never()).save(any(Enrollment.class));
    }

    @Test
    void returnsAllActiveLearnersAsCandidatesWhenNobodyIsEnrolled() {
        User learner = User.builder().id(3L).prenom("Ada").nom("Lovelace")
                .email("ada@example.test").role(Role.APPRENANT).active(true).build();
        when(courses.findById(8L)).thenReturn(Optional.of(Course.builder().id(8L).build()));
        when(enrollments.findByCourseId(8L)).thenReturn(List.of());
        when(users.findByRoleAndActiveTrue(Role.APPRENANT)).thenReturn(List.of(learner));

        EnrollmentCandidatesDTO result = service.getCourseCandidates(8L);

        assertEquals(0, result.enrolledCount());
        assertEquals(List.of(3L), result.learners().stream().map(learnerDto -> learnerDto.id()).toList());
        assertEquals(List.of(), result.enrolledLearners());
    }

    @Test
    void enrollsSeveralLearnersAndSkipsExistingEnrollments() {
        User alreadyEnrolled = User.builder().id(3L).role(Role.APPRENANT).active(true).build();
        User newLearner = User.builder().id(4L).role(Role.APPRENANT).active(true).build();
        Course course = Course.builder().id(8L).active(true).build();
        Enrollment existing = Enrollment.builder().user(alreadyEnrolled).course(course).build();
        when(courses.findById(8L)).thenReturn(Optional.of(course));
        when(users.findAllById(List.of(3L, 4L))).thenReturn(List.of(alreadyEnrolled, newLearner));
        when(enrollments.findByCourseId(8L)).thenReturn(List.of(existing));

        EnrollmentBatchResult result = service.enrollUsersToCourse(8L, List.of(3L, 4L));

        assertEquals(1, result.enrolledCount());
        assertEquals(1, result.alreadyEnrolledCount());
        verify(enrollments).saveAll(argThat(saved -> saved.spliterator().getExactSizeIfKnown() == 1));
    }
}
