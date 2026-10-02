package com.example.ODC_Academy.controller;

import com.example.ODC_Academy.auth.JwtResponseDTO;
import com.example.ODC_Academy.auth.LoginRequestDTO;
import com.example.ODC_Academy.auth.RegisterRequestDTO;
import com.example.ODC_Academy.auth.RegisterResponseDTO;
import com.example.ODC_Academy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Inscription et connexion des utilisateurs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Créer un nouveau compte utilisateur (apprenant, formateur ou administrateur)")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur et obtenir un jeton JWT")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
