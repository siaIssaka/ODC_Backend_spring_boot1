package com.example.ODC_Academy.security;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CourseContentAccessTest {
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final CourseContentAccess access = new CourseContentAccess(enrollments);
    private final User learner = User.builder().id(21L).role(Role.APPRENANT).build();
    private final Course course = Course.builder().id(34L).build();

    @Test
    void learnerWithoutEnrollmentCannotViewCourseContent() {
        when(enrollments.findByUserIdAndCourseId(21L, 34L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> access.assertCanView(learner, course));
    }

    @Test
    void onlyActiveEnrollmentGrantsAccess() {
        when(enrollments.findByUserIdAndCourseId(21L, 34L))
                .thenReturn(Optional.of(Enrollment.builder().status(EnrollmentStatus.PENDING).build()))
                .thenReturn(Optional.of(Enrollment.builder().status(EnrollmentStatus.ACTIVE).build()));

        assertThrows(AccessDeniedException.class, () -> access.assertCanView(learner, course));
        assertDoesNotThrow(() -> access.assertCanView(learner, course));
    }

    @Test
    void staffCanViewCourseContentWithoutLearnerEnrollment() {
        User admin = User.builder().id(22L).role(Role.ADMIN).build();
        User trainer = User.builder().id(23L).role(Role.FORMATEUR).build();

        assertDoesNotThrow(() -> access.assertCanView(admin, course));
        assertDoesNotThrow(() -> access.assertCanView(trainer, course));
        verifyNoInteractions(enrollments);
    }
}
