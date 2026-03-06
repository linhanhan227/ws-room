package com.chat.controller;

import com.chat.exception.AuthenticationException;
import com.chat.model.User;
import com.chat.service.UserService;
import com.chat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService, jwtUtil);
    }

    @Test
    void loginShouldRejectEmptyPassword() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .password("secret")
                .isAdmin(false)
                .build();

        when(userService.getUserByUsername("test")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                authController.login(Map.of("username", "test", "password", "")));
    }

    @Test
    void loginShouldUsePasswordFieldForAuthentication() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .password("secret")
                .isAdmin(false)
                .build();

        when(userService.getUserByUsername("test")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("u1", "test", false)).thenReturn("token");

        Object response = authController.login(Map.of("username", "test", "password", "secret")).getBody();
        assertEquals(Map.of("message", "登录成功", "token", "token", "userId", "u1", "username", "test", "isAdmin", false), response);
    }
}
