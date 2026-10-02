package com.example.ODC_Academy.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Accès base de données pour l'entité {@link User}.
 * Spring Data JPA génère automatiquement les requêtes à partir des noms de méthodes.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Recherche un utilisateur par email (utilisé au login et pour /users/me). */
    Optional<User> findByEmail(String email);

    /** Vérifie si un email est déjà pris (inscription). */
    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleSub(String googleSub);

    List<User> findByRoleAndActiveTrue(Role role);
}
