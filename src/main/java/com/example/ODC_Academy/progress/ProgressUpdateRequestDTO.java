package com.example.ODC_Academy.progress;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProgressUpdateRequestDTO(

        @NotNull(message = "Le nombre de leçons complétées est obligatoire")
        @Min(value = 0, message = "Le nombre de leçons complétées ne peut pas être négatif")
        Integer completedLessons,

        @NotNull(message = "Le nombre total de leçons est obligatoire")
        @Min(value = 0, message = "Le nombre total de leçons ne peut pas être négatif")
        Integer totalLessons
) {
}
