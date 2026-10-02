package com.example.ODC_Academy.service;

import com.example.ODC_Academy.auth.RegisterRequestDTO;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.settings.RegistrationSettingsService;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.example.ODC_Academy.security.JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RegistrationSettingsService registrationSettings;

    @InjectMocks
    private AuthService authService;

    @Test
    void publicRegistrationRejectsAdminRole() {
        RegisterRequestDTO request = new RegisterRequestDTO("Jean", "Dupont", "admin@test.com", "secret123", Role.ADMIN);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));

        assertTrue(ex.getMessage().contains("ADMIN"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void publicRegistrationRejectsFormateurRole() {
        RegisterRequestDTO request = new RegisterRequestDTO("Alice", "Martin", "alice@test.com", "secret123", Role.FORMATEUR);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));

        assertTrue(ex.getMessage().contains("FORMATEUR"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void publicRegistrationIsRejectedWhenRegistrationIsClosed() {
        org.mockito.Mockito.when(registrationSettings.isOpen()).thenReturn(false);
        RegisterRequestDTO request = new RegisterRequestDTO("Jean", "Dupont", "jean@test.com", "secret123", Role.APPRENANT);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));

        assertTrue(ex.getMessage().contains("Aucune formation n'est ouverte"));
        verify(userRepository, never()).save(any(User.class));
    }
}
