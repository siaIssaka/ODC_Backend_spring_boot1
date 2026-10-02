package com.example.ODC_Academy.admin;

import com.example.ODC_Academy.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequestDTO(
        @NotBlank(message = "Le prénom est obligatoire") String prenom,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Le format de l'email est invalide") String email,
        @NotBlank(message = "Le mot de passe est obligatoire") @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String password,
        @NotNull(message = "Le rôle est obligatoire") Role role
) {
}
