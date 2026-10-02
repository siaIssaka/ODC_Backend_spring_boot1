package com.example.ODC_Academy.module;

import jakarta.validation.constraints.NotBlank;

public record ModuleRequestDTO(
        @NotBlank(message = "Le titre du module est obligatoire")
        String title,

        String description,

        Integer orderIndex
) {
}
