package com.chat.util;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final UserRepository userRepository;

    @Autowired
    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userId, String username, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("isAdmin", isAdmin);
        claims.put("tokenVersion", getCurrentTokenVersion(userId));
        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("userId", String.class) : null;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    public boolean isAdminFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null && claims.get("isAdmin", Boolean.class);
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String userId = claims.getSubject();
            long tokenVersion = getTokenVersion(claims);
            long currentVersion = getCurrentTokenVersion(userId);
            if (tokenVersion != currentVersion) {
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            if (claims != null) {
                invalidateAllTokensForUserIfVersionMatches(claims.getSubject(), getTokenVersion(claims));
            }
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    public void invalidateAllTokensForUser(String userId) {
        invalidateAllTokensForUserIfVersionMatches(userId, null);
    }

    private void invalidateAllTokensForUserIfVersionMatches(String userId, Long expectedVersion) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        userRepository.findByUserId(userId).ifPresent(user -> {
            long currentVersion = user.getTokenVersion() == null ? 0L : user.getTokenVersion();
            if (expectedVersion != null && expectedVersion != currentVersion) {
                return;
            }
            user.setTokenVersion(currentVersion + 1);
            userRepository.save(user);
        });
    }

    private long getCurrentTokenVersion(String userId) {
        return userRepository.findByUserId(userId)
                .map(User::getTokenVersion)
                .map(version -> version == null ? 0L : version)
                .orElse(0L);
    }

    private long getTokenVersion(Claims claims) {
        if (claims == null) {
            return 0L;
        }
        Object rawVersion = claims.get("tokenVersion");
        if (rawVersion instanceof Number) {
            return ((Number) rawVersion).longValue();
        }
        return 0L;
    }
}
