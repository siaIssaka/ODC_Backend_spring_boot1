package com.example.ODC_Academy.auth;

public record RegisterResponseDTO(
        String message
) {
    public static RegisterResponseDTO of(String message) {
        return new RegisterResponseDTO(message);
    }
}
