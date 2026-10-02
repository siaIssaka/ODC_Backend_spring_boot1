package com.example.ODC_Academy.auth;

import com.example.ODC_Academy.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Jeton de réinitialisation : seule l'empreinte SHA-256 est stockée (une fuite de base ne révèle aucun lien valide). */
@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
