package com.example.ODC_Academy.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des comptes utilisateurs.
 * - GET /me : tout utilisateur authentifié (son propre profil via SecurityContext)
 * - GET / et GET /{id} : ADMIN uniquement
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Utilisateurs", description = "Gestion des comptes et profil courant")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Profil de l'utilisateur authentifié")
    public UserDTO getCurrentUser(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        return UserMapper.toDto(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Liste de tous les utilisateurs (ADMIN)")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(UserMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Détail d'un utilisateur par id (ADMIN)")
    public UserDTO getUserById(@PathVariable Long id) {
        return UserMapper.toDto(userService.getUserById(id));
    }
}
