package com.example.ODC_Academy.admin;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequestDTO(
        @NotNull(message = "Le statut actif est obligatoire") Boolean active
) {
}
