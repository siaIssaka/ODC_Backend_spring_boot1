package com.example.ODC_Academy.lesson;

public record LessonDTO(
        Long id,
        String title,
        String content,
        Long courseId,
        Long moduleId,
        String videoUrl,
        String documentUrl,
        Integer durationMinutes,
        Integer orderIndex
) {
}
