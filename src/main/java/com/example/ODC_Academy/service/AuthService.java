package com.example.ODC_Academy.service;

import com.example.ODC_Academy.auth.JwtResponseDTO;
import com.example.ODC_Academy.auth.LoginRequestDTO;
import com.example.ODC_Academy.auth.RegisterRequestDTO;
import com.example.ODC_Academy.auth.RegisterResponseDTO;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.DuplicateResourceException;
import com.example.ODC_Academy.security.JwtService;
import com.example.ODC_Academy.settings.RegistrationSettingsService;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RegistrationSettingsService registrationSettings;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager,
                        RegistrationSettingsService registrationSettings) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.registrationSettings = registrationSettings;
    }

    public RegisterResponseDTO register(RegisterRequestDTO request) {
        // L'inscription publique est strictement réservée aux apprenants.
        // On ignore toute tentative de création d'ADMIN ou FORMATEUR depuis l'interface publique.
        Role requestedRole = request.role();
        if (requestedRole == Role.ADMIN || requestedRole == Role.FORMATEUR) {
            throw new BadRequestException(
                    "L'inscription publique est réservée aux apprenants. Le rôle ADMIN et FORMATEUR ne sont pas autorisés.");
        }

        if (!registrationSettings.isOpen()) {
            throw new BadRequestException(RegistrationSettingsService.CLOSED_MESSAGE);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Un compte existe déjà avec l'email : " + request.email());
        }

        User user = User.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.APPRENANT)
                .active(true)
                .build();

        userRepository.save(user);
        return RegisterResponseDTO.of("Utilisateur créé avec succès. Veuillez vous connecter pour obtenir un token.");
    }

    public JwtResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Incohérence : utilisateur authentifié mais introuvable"));

        return tokenFor(user);
    }

    /** Émet un JWT pour un utilisateur déjà authentifié par un autre moyen (ex. Google). */
    public JwtResponseDTO tokenFor(User user) {
        String token = jwtService.generateToken(toUserDetails(user));
        return JwtResponseDTO.of(token, jwtService.getExpirationMs());
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
