package co.ravn.ecommerce.Services.Auth;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${app.jwt-secret}")
    private String SECRET_KEY;

    @Value("${app.jwt-expiration-minutes}")
    private long ACCESS_TOKEN_EXPIRY; // 15 minutes
    @Value("${app.refresh-token-expiration-days}")
    private long REFRESH_TOKEN_EXPIRY; // 7 days

    public String generateAccessToken(SysUser sysUser) {
        return createToken(new HashMap<>(), sysUser.getUsername(), ACCESS_TOKEN_EXPIRY);
    }

    public LocalDateTime getRefreshTokenExpiry() {
        return Instant.ofEpochMilli(
                        new Date().getTime() + REFRESH_TOKEN_EXPIRY
                ).atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String generateRefreshToken(SysUser sysUser) {
        return createToken(new HashMap<>(), sysUser.getUsername(), REFRESH_TOKEN_EXPIRY);
    }

    public String generatePasswordResetToken(SysUser sysUser) {
        return createToken(new HashMap<>(), sysUser.getUsername(), 15 * 60 * 1000); // 15 minutes
    }

    private String createToken(Map<String, Object> claims, String subject, long expiry) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, SysUser sysUser) {
        final String username = extractUsername(token);
        return (username.equals(sysUser.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenValid(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
