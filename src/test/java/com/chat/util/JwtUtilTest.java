package com.chat.util;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private UserRepository userRepository;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(userRepository);
        ReflectionTestUtils.setField(jwtUtil, "secret", "your-secret-key-change-this-in-production-environment-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);
    }

    @Test
    void invalidateAllTokensForUserShouldInvalidatePreviouslyIssuedTokens() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .tokenVersion(0L)
                .build();
        when(userRepository.findByUserId("u1")).thenAnswer(invocation -> Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String tokenA = jwtUtil.generateToken("u1", "test", false);
        String tokenB = jwtUtil.generateToken("u1", "test", false);
        assertTrue(jwtUtil.validateToken(tokenA));
        assertTrue(jwtUtil.validateToken(tokenB));

        jwtUtil.invalidateAllTokensForUser("u1");

        assertFalse(jwtUtil.validateToken(tokenA));
        assertFalse(jwtUtil.validateToken(tokenB));
    }

    @Test
    void validatingExpiredTokenShouldInvalidateAllUserTokens() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .tokenVersion(0L)
                .build();
        when(userRepository.findByUserId("u1")).thenAnswer(invocation -> Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String secret = "your-secret-key-change-this-in-production-environment-at-least-256-bits-long";
        SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        String shortLivedToken = Jwts.builder()
                .claims(Map.of("userId", "u1", "username", "test", "isAdmin", false, "tokenVersion", 0L))
                .subject("u1")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(signingKey)
                .compact();

        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);
        String normalToken = jwtUtil.generateToken("u1", "test", false);

        assertFalse(jwtUtil.validateToken(shortLivedToken));
        assertFalse(jwtUtil.validateToken(normalToken));
    }
}
