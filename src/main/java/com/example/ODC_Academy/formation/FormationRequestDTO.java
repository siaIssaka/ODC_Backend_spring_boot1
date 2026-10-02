package com.example.ODC_Academy.formation;

import jakarta.validation.constraints.NotBlank;

public record FormationRequestDTO(
        @NotBlank(message = "Le titre de la formation est obligatoire") String title,
        String description,
        Long categoryId) {
}
