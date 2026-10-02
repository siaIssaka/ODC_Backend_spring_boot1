package com.example.ODC_Academy.lesson;

import com.example.ODC_Academy.lesson.LessonDTO;
import com.example.ODC_Academy.lesson.Lesson;

public final class LessonMapper {

    private LessonMapper() {
    }

    public static LessonDTO toDto(Lesson lesson) {
        return new LessonDTO(lesson.getId(), lesson.getTitle(), lesson.getContent(),
                lesson.getCourse().getId(),
                lesson.getModule() != null ? lesson.getModule().getId() : null,
                lesson.getVideoUrl(), lesson.getDocumentUrl(), lesson.getDurationMinutes(), lesson.getOrderIndex());
    }
}
