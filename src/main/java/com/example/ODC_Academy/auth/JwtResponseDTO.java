package com.example.ODC_Academy.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private long expiresIn;

    public static JwtResponseDTO of(String token, long expiresIn) {
        JwtResponseDTO dto = new JwtResponseDTO();
        dto.setToken(token);
        dto.setType("Bearer");
        dto.setExpiresIn(expiresIn);
        return dto;
    }
}
