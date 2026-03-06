package com.chat.service;

import com.chat.model.Room;
import com.chat.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository);
    }

    @Test
    void updatePasswordShouldHandleNullCurrentPassword() {
        Room room = Room.builder()
                .roomId("r1")
                .name("test")
                .isPrivate(true)
                .password(null)
                .build();

        when(roomRepository.findByRoomId("r1")).thenReturn(Optional.of(room));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roomService.updatePassword("r1", "old-pass", "new-pass"));

        assertEquals("旧密码错误", ex.getMessage());
    }
}
