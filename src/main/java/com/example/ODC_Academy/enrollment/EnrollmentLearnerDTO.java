package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.UserDTO;

import java.time.LocalDateTime;
import java.util.List;

public record EnrollmentLearnerDTO(UserDTO learner, List<Long> courseIds, EnrollmentStatus status, LocalDateTime enrolledAt) {
}
