package com.example.ODC_Academy.auth;

import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.notification.Notifier;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/** Réinitialisation du mot de passe : jeton aléatoire 256 bits, valable 30 min, usage unique. */
@Service
public class PasswordResetService {
    static final Duration TTL = Duration.ofMinutes(30);
    static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final Notifier notifier;
    private final String publicUrl;

    public PasswordResetService(PasswordResetTokenRepository tokens, UserRepository users, PasswordEncoder encoder,
                                Notifier notifier, @Value("${app.public-url:http://localhost:4200}") String publicUrl) {
        this.tokens = tokens; this.users = users; this.encoder = encoder; this.notifier = notifier; this.publicUrl = publicUrl;
    }

    /** Ne révèle jamais si l'adresse existe : le contrôleur répond toujours pareil. Un envoi par minute et par compte. */
    @Transactional
    public void request(String email) {
        LocalDateTime now = LocalDateTime.now();
        tokens.deleteByExpiresAtBefore(now);
        users.findByEmailIgnoreCase(email.trim()).filter(User::isActive).ifPresent(u -> {
            if (tokens.existsByUserIdAndCreatedAtAfter(u.getId(), now.minus(COOLDOWN))) return;
            tokens.deleteByUserId(u.getId());
            byte[] raw = new byte[32];
            RANDOM.nextBytes(raw);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
            tokens.save(PasswordResetToken.builder().user(u).tokenHash(hash(token)).createdAt(now).expiresAt(now.plus(TTL)).build());
            notifier.passwordReset(u, publicUrl + "/reset-password?token=" + token);
        });
    }

    @Transactional
    public void reset(String token, String newPassword) {
        PasswordResetToken t = tokens.findByTokenHash(hash(token.trim()))
                .filter(x -> x.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BadRequestException("Lien invalide ou expiré. Refaites une demande."));
        User u = t.getUser();
        if (!u.isActive()) throw new BadRequestException("Lien invalide ou expiré. Refaites une demande.");
        u.setPassword(encoder.encode(newPassword)); // change l'empreinte « pv » : les anciens JWT deviennent invalides
        users.save(u);
        tokens.deleteByUserId(u.getId());
        notifier.passwordChanged(u);
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
