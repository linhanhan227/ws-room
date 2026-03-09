package com.chat.controller;

import com.chat.model.User;
import com.chat.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(adminService);
    }

    @Test
    void muteUserShouldSucceedWhenMutedUntilIsNull() {
        User user = User.builder()
                .userId("u1")
                .username("test")
                .isMuted(true)
                .build();
        user.setMutedUntil(null);
        when(adminService.muteUser("u1", "admin", 30)).thenReturn(user);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", "admin");

        ResponseEntity<?> response = adminController.muteUser(request, Map.of(
                "userId", "u1",
                "minutes", 30
        ));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        var body = (Map<String, Object>) response.getBody();
        assertNull(body.get("mutedUntil"));
    }
}
