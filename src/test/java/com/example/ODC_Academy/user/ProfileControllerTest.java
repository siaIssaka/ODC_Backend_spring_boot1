package com.example.ODC_Academy.user;

import com.example.ODC_Academy.auth.JwtResponseDTO;
import com.example.ODC_Academy.exception.DuplicateResourceException;
import com.example.ODC_Academy.media.MediaStorageService;
import com.example.ODC_Academy.security.SecurityUtils;
import com.example.ODC_Academy.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {
    @Mock private SecurityUtils security;
    @Mock private UserRepository users;
    @Mock private MediaStorageService storage;
    @Mock private AuthService authService;
    @InjectMocks private ProfileController controller;

    @Test
    void updatesOwnNameAndEmailAndReturnsReplacementToken() {
        User learner = User.builder().id(4L).nom("Ancien").prenom("Nom").email("old@example.com")
                .password("hash").role(Role.APPRENANT).active(true).build();
        when(security.getCurrentUser()).thenReturn(learner);
        when(users.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(users.save(learner)).thenReturn(learner);
        when(authService.tokenFor(learner)).thenReturn(JwtResponseDTO.of("new-token", 60_000));

        ProfileUpdateResponseDTO result = controller.update(
                new ProfileUpdateRequestDTO("  Doe ", " Jane ", " New@Example.com "));

        assertEquals("Doe", result.user().nom());
        assertEquals("Jane", result.user().prenom());
        assertEquals("new@example.com", result.user().email());
        assertEquals("new-token", result.token());
        verify(users).save(learner);
    }

    @Test
    void rejectsAnEmailOwnedByAnotherAccount() {
        User learner = User.builder().id(4L).email("old@example.com").build();
        User other = User.builder().id(5L).email("taken@example.com").build();
        when(security.getCurrentUser()).thenReturn(learner);
        when(users.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(other));

        assertThrows(DuplicateResourceException.class, () -> controller.update(
                new ProfileUpdateRequestDTO("Doe", "Jane", "taken@example.com")));
    }
}
