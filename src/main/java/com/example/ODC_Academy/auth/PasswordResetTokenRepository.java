package com.example.ODC_Academy.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime after);
    void deleteByUserId(Long userId);
    void deleteByExpiresAtBefore(LocalDateTime t);
}
