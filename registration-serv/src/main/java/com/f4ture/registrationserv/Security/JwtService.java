package com.f4ture.registrationserv.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("type", "FULL")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000L))
                .signWith(getSigningKey())
                .compact();
    }

    public String generatePreAuthToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("type", "PRE_AUTH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300000L))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmailFromPreAuth(String token) {
        Claims claims = extractClaims(token);
        if (!"PRE_AUTH".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Invalid token type");
        }
        return claims.getSubject();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
