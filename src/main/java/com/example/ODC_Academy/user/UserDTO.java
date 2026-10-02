package com.example.ODC_Academy.user;

import java.time.LocalDateTime;

/**
 * Données utilisateur exposées par l'API (sans le mot de passe).
 * Correspond au JSON renvoyé par GET /api/v1/users/me et GET /api/v1/users.
 */
public record UserDTO(
        Long id,
        String nom,
        String prenom,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt,
        String photoKey
) {
}
