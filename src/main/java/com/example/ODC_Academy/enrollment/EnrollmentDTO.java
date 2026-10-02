package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.status.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentDTO(
        Long id,
        Long userId,
        Long courseId,
        String courseTitle,
        Long sessionId,
        String sessionName,
        java.time.LocalDateTime sessionStartsAt,
        java.time.LocalDateTime sessionEndsAt,
        EnrollmentStatus status,
        LocalDateTime enrolledAt
) {
}
