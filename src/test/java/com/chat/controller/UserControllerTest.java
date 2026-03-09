package com.chat.controller;

import com.chat.model.User;
import com.chat.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void getUserByIdShouldIncludeAvatarAndSignature() {
        User user = User.builder()
                .userId("123456")
                .username("test")
                .isOnline(true)
                .isAdmin(false)
                .isMuted(false)
                .roomId("r1")
                .avatar("http://api.tos.tiecode.org.cn/a.jpg")
                .signature("hello")
                .build();
        when(userService.getUserById("123456")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserById("123456");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertEquals("http://api.tos.tiecode.org.cn/a.jpg", body.get("avatar"));
        assertEquals("hello", body.get("signature"));
    }

    @Test
    void getUserByIdShouldReturnBadRequestForNonNumericId() {
        ResponseEntity<?> response = userController.getUserById("abc123");

        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        var body = (Map<String, String>) response.getBody();
        assertEquals("用户ID格式无效", body.get("error"));
        verifyNoInteractions(userService);
    }

    @Test
    void getUserByIdShouldReturnBadRequestForTooLongNumericId() {
        ResponseEntity<?> response = userController.getUserById("1234567890123456789012345678901234567890");

        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        var body = (Map<String, String>) response.getBody();
        assertEquals("用户ID格式无效", body.get("error"));
        verifyNoInteractions(userService);
    }

    @Test
    void updateAvatarShouldReturnForbiddenWhenRequestUserIdMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = userController.updateAvatar(request, "u1", Map.of("avatar", "a.jpg"));

        assertEquals(403, response.getStatusCode().value());
        verifyNoInteractions(userService);
    }

    @Test
    void updateSignatureShouldReturnForbiddenWhenRequestUserIdMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = userController.updateSignature(request, "u1", Map.of("signature", "hello"));

        assertEquals(403, response.getStatusCode().value());
        verifyNoInteractions(userService);
    }

    @Test
    void updateProfileShouldReturnForbiddenWhenRequestUserIdMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = userController.updateProfile(request, "u1", Map.of("avatar", "a.jpg", "signature", "hello"));

        assertEquals(403, response.getStatusCode().value());
        verifyNoInteractions(userService);
    }
}
