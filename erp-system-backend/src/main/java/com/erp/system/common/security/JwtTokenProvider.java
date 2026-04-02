package com.erp.system.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Long userId, String username, List<String> roles) {
        return buildToken(userId, username, roles, "ACCESS", jwtProperties.getExpirationMs());
    }

    public String generateRefreshToken(Long userId, String username, List<String> roles) {
        return buildToken(userId, username, roles, "REFRESH", jwtProperties.getRefreshExpirationMs());
    }

    public JwtClaims getClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new JwtClaims(
                Long.parseLong(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("roles", List.class),
                claims.get("tokenUse", String.class),
                claims.getExpiration()
        );
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(getClaims(token).tokenUse());
    }

    public boolean isAccessToken(String token) {
        return "ACCESS".equals(getClaims(token).tokenUse());
    }

    private String buildToken(Long userId, String username, List<String> roles, String tokenUse, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .claim("tokenUse", tokenUse)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record JwtClaims(Long userId, String username, List<String> roles, String tokenUse, Date expiration) {
    }
}
