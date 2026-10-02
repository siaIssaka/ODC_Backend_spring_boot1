package com.example.ODC_Academy.admin;

import com.example.ODC_Academy.auth.RegisterRequestDTO;
import com.example.ODC_Academy.auth.RegisterResponseDTO;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.DuplicateResourceException;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponseDTO createAdmin(RegisterRequestDTO request) {
        if (request.role() != Role.ADMIN) {
            throw new BadRequestException("Cet endpoint permet uniquement de créer un compte ADMIN.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Un compte existe déjà avec l'email : " + request.email());
        }

        User user = User.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(user);
        return RegisterResponseDTO.of("Compte administrateur créé avec succès.");
    }

    public User createManagedUser(AdminCreateUserRequestDTO request) {
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Cet endpoint ne permet pas de créer un compte ADMIN.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Un compte existe déjà avec l'email : " + request.email());
        }

        User user = User.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .build();
        return userRepository.save(user);
    }

    public User setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));
        user.setActive(active);
        return userRepository.save(user);
    }
}
