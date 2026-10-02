package com.example.ODC_Academy.enrollment;

import com.example.ODC_Academy.user.UserDTO;

import java.util.List;

public record EnrollmentCandidatesDTO(
        int enrolledCount,
        List<UserDTO> learners,
        List<EnrollmentLearnerDTO> enrolledLearners
) {
}
