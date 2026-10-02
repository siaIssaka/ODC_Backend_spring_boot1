package com.example.ODC_Academy.lesson;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;

public record LessonRequestDTO(

        @NotBlank(message = "Le titre de la leçon est obligatoire")
        String title,

        String content,

        Long moduleId,

        @Pattern(regexp = "^$|https?://.+", message = "L'URL vidéo doit commencer par http:// ou https://")
        String videoUrl,

        @Pattern(regexp = "^$|https?://.+", message = "L'URL du document doit commencer par http:// ou https://")
        String documentUrl,

        @Positive(message = "La durée doit être supérieure à zéro")
        Integer durationMinutes,

        @PositiveOrZero(message = "L'ordre doit être positif ou nul")
        Integer orderIndex
) {
}
