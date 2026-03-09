package com.chat.controller;

import com.chat.model.User;
import com.chat.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                .userId("u1")
                .username("test")
                .isOnline(true)
                .isAdmin(false)
                .isMuted(false)
                .roomId("r1")
                .avatar("http://api.tos.tiecode.org.cn/a.jpg")
                .signature("hello")
                .build();
        when(userService.getUserById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserById("u1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertEquals("http://api.tos.tiecode.org.cn/a.jpg", body.get("avatar"));
        assertEquals("hello", body.get("signature"));
    }
}
