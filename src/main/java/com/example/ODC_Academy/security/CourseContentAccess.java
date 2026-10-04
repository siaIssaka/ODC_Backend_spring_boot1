package com.example.ODC_Academy.security;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class CourseContentAccess {
    private final EnrollmentRepository enrollments;

    public CourseContentAccess(EnrollmentRepository enrollments) {
        this.enrollments = enrollments;
    }

    public void assertCanView(User viewer, Course course) {
        if (viewer.getRole() != Role.APPRENANT) return;

        boolean activelyEnrolled = enrollments.findByUserIdAndCourseId(viewer.getId(), course.getId())
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .isPresent();
        if (!activelyEnrolled) {
            throw new AccessDeniedException("Une inscription active est requise pour accéder au contenu de ce cours");
        }
    }
}
