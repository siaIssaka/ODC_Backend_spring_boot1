package com.example.ODC_Academy.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Réinitialisation du mot de passe, connexion Google et configuration publique (sous /api/v1/auth/**, public). */
@RestController
@RequestMapping("/api/v1/auth")
public class AccountController {
    public record ForgotRequest(@NotBlank @Email String email) { }
    public record ResetRequest(@NotBlank String token,
                               @NotBlank @Size(min = 8, max = 128, message = "Le mot de passe doit contenir au moins 8 caractères") String newPassword) { }
    public record GoogleRequest(@NotBlank String credential) { }
    public record AuthConfig(String googleClientId) { }

    private final PasswordResetService reset;
    private final GoogleAuthService google;

    public AccountController(PasswordResetService reset, GoogleAuthService google) { this.reset = reset; this.google = google; }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> forgot(@Valid @RequestBody ForgotRequest r) {
        reset.request(r.email());
        return Map.of("message", "Si un compte existe pour cette adresse, un e-mail de réinitialisation vient d'être envoyé.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody ResetRequest r) { reset.reset(r.token(), r.newPassword()); }

    @PostMapping("/google")
    public JwtResponseDTO google(@Valid @RequestBody GoogleRequest r) { return google.login(r.credential()); }

    @GetMapping("/config")
    public AuthConfig config() { return new AuthConfig(google.clientId()); }
}
