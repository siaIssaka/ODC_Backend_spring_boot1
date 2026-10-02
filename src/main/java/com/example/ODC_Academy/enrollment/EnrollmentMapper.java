package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.enrollment.EnrollmentDTO;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentDTO toDto(Enrollment enrollment) {
        return new EnrollmentDTO(
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourseSession() == null ? null : enrollment.getCourseSession().getId(),
                enrollment.getCourseSession() == null ? null : enrollment.getCourseSession().getName(),
                enrollment.getCourseSession() == null ? null : enrollment.getCourseSession().getStartsAt(),
                enrollment.getCourseSession() == null ? null : enrollment.getCourseSession().getEndsAt(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}
