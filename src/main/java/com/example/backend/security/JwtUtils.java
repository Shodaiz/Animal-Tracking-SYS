package com.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final String SECRET =
            "dzcheptelSecretKey2024dzcheptelSecretKey2024dzcheptelSecretKey2024";
    private static final long EXPIRATION_MS = 86400000; // 24 heures

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Génère un token JWT avec username et role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrait le username du token
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // Extrait le role du token
    public String getRoleFromToken(String token) {
        return (String) parseClaims(token).get("role");
    }

    // Valide le token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}