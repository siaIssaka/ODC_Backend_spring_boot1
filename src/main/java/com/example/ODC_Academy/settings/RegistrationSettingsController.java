package com.example.ODC_Academy.settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings/registration")
public class RegistrationSettingsController {
    public record RegistrationStatus(boolean open) { }
    public record UpdateRegistrationRequest(@NotNull Boolean open) { }

    private final RegistrationSettingsService registrationSettings;

    public RegistrationSettingsController(RegistrationSettingsService registrationSettings) {
        this.registrationSettings = registrationSettings;
    }

    @GetMapping
    public RegistrationStatus get() {
        return new RegistrationStatus(registrationSettings.isOpen());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RegistrationStatus update(@Valid @RequestBody UpdateRegistrationRequest request) {
        return new RegistrationStatus(registrationSettings.setOpen(request.open()));
    }
}
