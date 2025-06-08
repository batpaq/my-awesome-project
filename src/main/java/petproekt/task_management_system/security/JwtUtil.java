package petproekt.task_management_system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import petproekt.task_management_system.config.JwtProperties;
import petproekt.task_management_system.enm.TokenType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public String generateToken(String username, TokenType tokenType) {
        Date now = new Date();
        Date expiryDate;
        SecretKey key;

        if (tokenType == TokenType.ACCESS) {
            expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
            key = getAccessTokenKey();
        } else {
            expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());
            key = getRefreshTokenKey();
        }

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", tokenType.name())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token, TokenType tokenType) {
        Claims claims = parseClaims(token, tokenType);
        verifyTokenType(claims, tokenType);
        return claims.getSubject();
    }

    public boolean validateToken(String token, TokenType tokenType) {
        try {
            Claims claims = parseClaims(token, tokenType);
            verifyTokenType(claims, tokenType);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRemainingExpirationSeconds(String token, TokenType tokenType) {
        try {
            Claims claims = parseClaims(token, tokenType);
            Date expiration = claims.getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();
            return Math.max(diff / 1000, 0);
        } catch (JwtException | IllegalArgumentException e) {
            return 0;
        }
    }

    // --- Получение времени жизни access токена в миллисекундах ---
    public long getAccessTokenExpiration() {
        return jwtProperties.getAccessTokenExpiration();
    }

    // --- Получение времени жизни refresh токена в миллисекундах ---
    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
    }

    // --- Получение времени жизни access токена в минутах ---
    public long getAccessTokenExpirationMinutes() {
        return jwtProperties.getAccessTokenExpiration() / 60000;
    }

    // --- Получение времени жизни refresh токена в минутах ---
    public long getRefreshTokenExpirationMinutes() {
        return jwtProperties.getRefreshTokenExpiration() / 60000;
    }


    private Claims parseClaims(String token, TokenType tokenType) {
        SecretKey key = tokenType == TokenType.ACCESS ? getAccessTokenKey() : getRefreshTokenKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void verifyTokenType(Claims claims, TokenType expectedType) {
        String type = claims.get("type", String.class);
        if (type == null || !type.equals(expectedType.name())) {
            throw new JwtException("Invalid token type");
        }
    }

    private SecretKey getAccessTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getAccessTokenSecret().getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getRefreshTokenSecret().getBytes(StandardCharsets.UTF_8));
    }
}
