package com.example.ODC_Academy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Génération et validation des jetons JWT (access token).
 * Le secret et la durée de validité sont externalisés dans application.yml
 * (voir application.security.jwt.secret / expiration).
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpirationMs;

    /** Empreinte du hash du mot de passe : change dès que le mot de passe change, ce qui révoque les jetons émis avant. */
    static final String PASSWORD_VERSION_CLAIM = "pv";

    static String passwordVersion(String passwordHash) {
        try {
            byte[] d = java.security.MessageDigest.getInstance("SHA-256")
                    .digest((passwordHash == null ? "" : passwordHash).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(d, 0, 8);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(PASSWORD_VERSION_CLAIM, passwordVersion(userDetails.getPassword()));
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        String pv = extractClaim(token, c -> c.get(PASSWORD_VERSION_CLAIM, String.class));
        return username.equals(userDetails.getUsername()) && userDetails.isEnabled() && !isTokenExpired(token)
                && passwordVersion(userDetails.getPassword()).equals(pv);
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
