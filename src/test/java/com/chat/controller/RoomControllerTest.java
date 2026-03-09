package com.chat.controller;

import com.chat.model.Room;
import com.chat.service.RoomService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    private RoomController roomController;

    @BeforeEach
    void setUp() {
        roomController = new RoomController(roomService);
    }

    @Test
    void getRoomByIdShouldSucceedWhenDescriptionIsNull() {
        Room room = Room.builder()
                .roomId("r1")
                .name("test")
                .creator("u1")
                .description(null)
                .maxUsers(50)
                .isPrivate(false)
                .isActive(true)
                .build();
        when(roomService.getRoomById("r1")).thenReturn(Optional.of(room));

        ResponseEntity<?> response = roomController.getRoomById("r1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        var body = (Map<String, Object>) response.getBody();
        assertNull(body.get("description"));
    }

    @Test
    void createRoomShouldSucceedWhenDescriptionIsNull() {
        Room room = Room.builder()
                .roomId("r1")
                .name("test")
                .creator("admin")
                .description(null)
                .maxUsers(50)
                .isPrivate(false)
                .isActive(true)
                .build();
        when(roomService.createRoom("test", "admin", null, 50, false, null)).thenReturn(room);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", "admin");

        ResponseEntity<?> response = roomController.createRoom(request, Map.of(
                "name", "test",
                "maxUsers", 50,
                "isPrivate", false
        ));

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        var body = (Map<String, Object>) response.getBody();
        assertNull(body.get("description"));
    }
}
