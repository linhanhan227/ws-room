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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void getLoginUsageHintShouldReturnMethodNotAllowed() {
        ResponseEntity<?> responseEntity = authController.getLoginUsageHint();
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
        Object response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(Map.of(
                "errorCode", "HTTP_405",
                "errorMessage", "登录仅支持POST请求",
                "errorDetails", "请使用POST /api/auth/login并在请求体中提交用户名和密码"
        ), response);
    }

    @Test
    void getLoginEndpointShouldReturn405WithUsageHint() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value("HTTP_405"))
                .andExpect(jsonPath("$.errorMessage").value("登录仅支持POST请求"))
                .andExpect(jsonPath("$.errorDetails").value("请使用POST /api/auth/login并在请求体中提交用户名和密码"));

        verifyNoInteractions(userService, jwtUtil);
    }

    @Test
    void logoutShouldInvalidateAllUserTokens() {
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("token")).thenReturn("u1");
        when(jwtUtil.getUsernameFromToken("token")).thenReturn("test");

        ResponseEntity<?> response = authController.logout("Bearer token");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("message", "退出登录成功", "username", "test"), response.getBody());
        verify(jwtUtil).invalidateAllTokensForUser("u1");
    }
}
