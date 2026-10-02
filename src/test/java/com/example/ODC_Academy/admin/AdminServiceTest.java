package com.example.ODC_Academy.admin;

import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    void createsTrainerWithHashedPasswordAndActiveStatus() {
        when(userRepository.existsByEmail("trainer@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        AdminCreateUserRequestDTO request = new AdminCreateUserRequestDTO(
                "Sam", "Trainer", "trainer@test.com", "password123", Role.FORMATEUR);

        User created = adminService.createManagedUser(request);

        assertEquals(Role.FORMATEUR, created.getRole());
        assertEquals("hashed", created.getPassword());
        assertTrue(created.isActive());
    }

    @Test
    void createsLearnerWithActiveStatus() {
        when(userRepository.existsByEmail("learner@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        AdminCreateUserRequestDTO request = new AdminCreateUserRequestDTO(
                "Alex", "Learner", "learner@test.com", "password123", Role.APPRENANT);

        User created = adminService.createManagedUser(request);

        assertEquals(Role.APPRENANT, created.getRole());
        assertTrue(created.isActive());
    }

    @Test
    void rejectsAdminRoleOnManagedUserEndpoint() {
        AdminCreateUserRequestDTO request = new AdminCreateUserRequestDTO(
                "Ada", "Admin", "admin@test.com", "password123", Role.ADMIN);

        assertThrows(BadRequestException.class, () -> adminService.createManagedUser(request));
    }

    @Test
    void updatesUserActiveStatus() {
        User user = User.builder().id(9L).nom("Martin").prenom("Alice")
                .email("alice@test.com").password("hash").role(Role.APPRENANT).active(true).build();
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = adminService.setUserActive(9L, false);

        assertFalse(updated.isActive());
        verify(userRepository).save(user);
    }
}
