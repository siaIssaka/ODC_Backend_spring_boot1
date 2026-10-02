package com.example.ODC_Academy.user;

/**
 * Conversion entité User → UserDTO.
 * On n'expose jamais le mot de passe haché dans les réponses API.
 */
public final class UserMapper {

    private UserMapper() {
        // utilitaire statique
    }

    public static UserDTO toDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getPhotoKey()
        );
    }
}
