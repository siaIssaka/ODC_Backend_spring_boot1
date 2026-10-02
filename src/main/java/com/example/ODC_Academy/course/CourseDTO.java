package com.example.ODC_Academy.course;

import java.time.LocalDateTime;

public record CourseDTO(
        Long id,
        Long formationId,
        String title,
        String description,
        String level,
        Double price,
        boolean active,
        LocalDateTime createdAt,
        Long createdById,
        String createdByName
) {
}
