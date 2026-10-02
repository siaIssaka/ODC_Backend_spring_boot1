package com.example.ODC_Academy.coursesession;

import java.time.LocalDateTime;
import java.util.List;

public record CourseSessionDTO(
        Long id,
        Long formationId,
        String formationTitle,
        String name,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        List<CourseSessionCourseDTO> courses,
        int enrolledCount
) {
}
