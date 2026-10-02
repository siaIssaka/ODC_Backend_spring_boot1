package com.example.ODC_Academy.security;

import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilitaires d'accès au principal authentifié et de contrôle d'ownership.
 */
@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return auth;
    }

    public String getCurrentEmail() {
        return getAuthentication().getName();
    }

    public User getCurrentUser() {
        return userRepository.findByEmail(getCurrentEmail())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur authentifié introuvable en base"));
    }

    public boolean isAdmin() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isFormateur() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FORMATEUR"));
    }

    public boolean isApprenant() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_APPRENANT"));
    }

    /**
     * Un utilisateur non-ADMIN ne peut accéder qu'à ses propres ressources (userId).
     * ADMIN peut tout.
     */
    public void assertSelfOrAdmin(Long targetUserId) {
        User current = getCurrentUser();
        if (current.getRole() == Role.ADMIN) {
            return;
        }
        if (!current.getId().equals(targetUserId)) {
            throw new AccessDeniedException(
                    "Accès refusé : vous ne pouvez accéder qu'à vos propres données");
        }
    }
}
