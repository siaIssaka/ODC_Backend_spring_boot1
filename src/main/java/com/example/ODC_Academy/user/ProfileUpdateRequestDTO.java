package com.example.ODC_Academy.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequestDTO(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
        String nom,
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 255, message = "Le prénom ne peut pas dépasser 255 caractères")
        String prenom,
        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail est invalide")
        @Size(max = 255, message = "L'adresse e-mail ne peut pas dépasser 255 caractères")
        String email
) {
}
