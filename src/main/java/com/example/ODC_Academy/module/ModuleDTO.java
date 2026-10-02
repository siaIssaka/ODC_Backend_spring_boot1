package com.example.ODC_Academy.module;

public record ModuleDTO(
        Long id,
        Long courseId,
        String title,
        String description,
        Integer orderIndex
) {
}
