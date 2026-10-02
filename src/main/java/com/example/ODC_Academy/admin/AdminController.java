package com.example.ODC_Academy.admin;

import com.example.ODC_Academy.auth.RegisterRequestDTO;
import com.example.ODC_Academy.auth.RegisterResponseDTO;
import com.example.ODC_Academy.user.UserDTO;
import com.example.ODC_Academy.user.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administration", description = "Gestion des comptes administrateurs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create-admin")
    @Operation(summary = "Créer un compte ADMIN (réservé aux administrateurs)")
    public ResponseEntity<RegisterResponseDTO> createAdmin(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(request));
    }

    @PostMapping("/users")
    @Operation(summary = "Créer un compte FORMATEUR ou APPRENANT")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody AdminCreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserMapper.toDto(adminService.createManagedUser(request)));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activer ou désactiver un compte utilisateur")
    public UserDTO setUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequestDTO request) {
        return UserMapper.toDto(adminService.setUserActive(id, request.active()));
    }
}
