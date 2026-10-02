package com.example.ODC_Academy.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseRequestDTO(

        @NotBlank(message = "Le titre du cours est obligatoire")
        String title,

        String description,

        @NotBlank(message = "Le niveau du cours est obligatoire")
        String level,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.0", inclusive = true, message = "Le prix ne peut pas être négatif")
        Double price
) {
}
