package com.chat.util;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    void expiredTokenShouldInvalidateAllTokensForSameUser() throws InterruptedException {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .tokenVersion(0L)
                .build();
        when(userRepository.findByUserId("u1")).thenAnswer(invocation -> Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(jwtUtil, "expiration", 5L);
        String shortLivedToken = jwtUtil.generateToken("u1", "test", false);

        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);
        String normalToken = jwtUtil.generateToken("u1", "test", false);

        Thread.sleep(30);

        assertFalse(jwtUtil.validateToken(shortLivedToken));
        assertFalse(jwtUtil.validateToken(normalToken));
    }
}
