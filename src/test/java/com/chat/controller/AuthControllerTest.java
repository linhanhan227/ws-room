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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    private AuthController authController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
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
    void loginShouldRejectWrongPassword() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .password("secret")
                .isAdmin(false)
                .build();

        when(userService.getUserByUsername("test")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                authController.login(Map.of("username", "test", "password", "wrong")));
    }

    @Test
    void loginShouldRejectWhenStoredPasswordIsNull() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .password(null)
                .isAdmin(false)
                .build();

        when(userService.getUserByUsername("test")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                authController.login(Map.of("username", "test", "password", "secret")));
    }

    @Test
    void loginShouldSucceedWithCorrectCredentials() {
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

    @Test
    void getLoginShouldSucceedWithCorrectCredentials() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .password("secret")
                .isAdmin(false)
                .build();

        when(userService.getUserByUsername("test")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("u1", "test", false)).thenReturn("token");

        Object response = authController.login("test", "secret").getBody();
        assertEquals(Map.of("message", "登录成功", "token", "token", "userId", "u1", "username", "test", "isAdmin", false), response);
    }

    @Test
    void getLoginEndpointShouldReturnSuccess() throws Exception {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .password("secret")
                .isAdmin(false)
                .build();

        when(userService.getUserByUsername("test")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("u1", "test", false)).thenReturn("token");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "test")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.isAdmin").value(false));
    }
}
