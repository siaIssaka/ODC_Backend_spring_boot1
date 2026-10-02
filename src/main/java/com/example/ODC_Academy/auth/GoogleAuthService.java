package com.example.ODC_Academy.auth;

import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.service.AuthService;
import com.example.ODC_Academy.settings.RegistrationSettingsService;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Connexion avec Google (Google Identity Services). Le navigateur obtient un ID token ; le serveur en vérifie la
 * signature (clés publiques Google), l'audience (notre client id), l'émetteur, l'expiration et l'e-mail vérifié.
 */
@Service
public class GoogleAuthService {
    private static final String JWKS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    private final String clientId;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthService authService;
    private final RegistrationSettingsService registrationSettings;
    private volatile NimbusJwtDecoder decoder;

    public GoogleAuthService(@Value("${app.google.client-id:}") String clientId, UserRepository users,
                             PasswordEncoder encoder, AuthService authService,
                             RegistrationSettingsService registrationSettings) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.users = users; this.encoder = encoder; this.authService = authService;
        this.registrationSettings = registrationSettings;
    }

    public boolean enabled() { return !clientId.isBlank(); }
    public String clientId() { return enabled() ? clientId : null; }

    private NimbusJwtDecoder decoder() {
        if (decoder == null) {
            NimbusJwtDecoder d = NimbusJwtDecoder.withJwkSetUri(JWKS).build();
            OAuth2TokenValidator<Jwt> issuer = jwt -> ISSUERS.contains(jwt.getClaimAsString("iss"))
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Émetteur invalide", null));
            OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audience invalide", null));
            d.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), issuer, audience));
            decoder = d;
        }
        return decoder;
    }

    @Transactional
    public com.example.ODC_Academy.auth.JwtResponseDTO login(String credential) {
        if (!enabled()) throw new BadRequestException("La connexion Google n'est pas configurée.");
        Jwt jwt;
        try {
            jwt = decoder().decode(credential);
        } catch (JwtException ex) {
            throw new BadRequestException("Jeton Google invalide ou expiré.");
        }
        Object verified = jwt.getClaim("email_verified");
        String email = jwt.getClaimAsString("email");
        if (email == null || !(Boolean.TRUE.equals(verified) || "true".equals(verified)))
            throw new BadRequestException("Votre adresse e-mail Google n'est pas vérifiée.");
        String sub = jwt.getSubject();

        User user = users.findByGoogleSub(sub).orElseGet(() -> users.findByEmailIgnoreCase(email).orElse(null));
        if (user == null) {
            if (!registrationSettings.isOpen()) {
                throw new BadRequestException(RegistrationSettingsService.CLOSED_MESSAGE);
            }
            user = users.save(User.builder()
                    .prenom(firstNonBlank(jwt.getClaimAsString("given_name"), jwt.getClaimAsString("name"), "Utilisateur"))
                    .nom(firstNonBlank(jwt.getClaimAsString("family_name"), "Google"))
                    .email(email.toLowerCase())
                    .password(encoder.encode(UUID.randomUUID() + UUID.randomUUID().toString())) // inutilisable : connexion via Google
                    .role(Role.APPRENANT).active(true).googleSub(sub).build());
        } else {
            if (!user.isActive()) throw new BadRequestException("Ce compte est désactivé.");
            if (user.getGoogleSub() != null && !user.getGoogleSub().equals(sub))
                throw new BadRequestException("Cette adresse est liée à un autre compte Google.");
            if (user.getGoogleSub() == null) { user.setGoogleSub(sub); users.save(user); } // liaison : e-mail vérifié par Google
        }
        return authService.tokenFor(user);
    }

    private static String firstNonBlank(String... v) {
        for (String s : v) if (s != null && !s.isBlank()) return s.trim();
        return "";
    }
}
