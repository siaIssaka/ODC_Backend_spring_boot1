package com.example.ODC_Academy.coursesession;

import com.example.ODC_Academy.enrollment.EnrollmentLearnerDTO;

import java.time.LocalDateTime;
import java.util.List;

public record AdminCohortOverviewDTO(
        Long id,
        String formationTitle,
        String name,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        List<CourseSessionCourseDTO> courses,
        List<EnrollmentLearnerDTO> enrolledLearners
) {
}
