package com.crmventas.api.security.jwt;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        ));
    }

    public String generateToken(String usuarioId, String email, String rol, boolean debeCambiarPass) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
            .id(jti)
            .subject(usuarioId)
            .claim("email", email)
            .claim("rol", rol)
            .claim("debe_cambiar_pass", debeCambiarPass)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public String getRol(String token) {
        return parseToken(token).get("rol", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
