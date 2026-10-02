package com.example.ODC_Academy.user;

import com.example.ODC_Academy.media.MediaStorageService;
import com.example.ODC_Academy.security.SecurityUtils;
import com.example.ODC_Academy.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/** Photo de profil de l'utilisateur connecté (hors /users/** réservé à l'ADMIN). */
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final SecurityUtils security;
    private final UserRepository users;
    private final MediaStorageService storage;
    private final AuthService authService;

    public ProfileController(SecurityUtils s, UserRepository u, MediaStorageService m, AuthService authService) {
        this.security = s; this.users = u; this.storage = m; this.authService = authService;
    }

    @PutMapping
    public ProfileUpdateResponseDTO update(@Valid @RequestBody ProfileUpdateRequestDTO request) {
        User me = security.getCurrentUser();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        users.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(me.getId()))
                .ifPresent(existing -> {
                    throw new com.example.ODC_Academy.exception.DuplicateResourceException(
                            "Cette adresse e-mail est déjà utilisée par un autre compte.");
                });

        me.setNom(request.nom().trim());
        me.setPrenom(request.prenom().trim());
        me.setEmail(email);
        User updated = users.save(me);
        return new ProfileUpdateResponseDTO(UserMapper.toDto(updated), authService.tokenFor(updated).getToken());
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTO uploadPhoto(@RequestParam("file") MultipartFile file) {
        User me = security.getCurrentUser();
        String key = storage.storeImage(file);
        if (me.getPhotoKey() != null) storage.delete(me.getPhotoKey());
        me.setPhotoKey(key);
        return UserMapper.toDto(users.save(me));
    }
}
