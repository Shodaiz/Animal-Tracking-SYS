package com.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    // ✅ Clé suffisamment longue pour HS256 (minimum 32 caractères)
    private static final String SECRET =
            "dzcheptelSecretKey2024dzcheptelSecretKey2024dzcheptelSecretKey2024";
    private static final long EXPIRATION_MS = 86400000L; // 24 heures

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ✅ API JJWT 0.12.x : subject() au lieu de setSubject(), etc.
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ API JJWT 0.12.x : verifyWith() au lieu de setSigningKey()
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}