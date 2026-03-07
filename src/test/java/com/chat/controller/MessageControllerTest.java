package com.chat.controller;

import com.chat.model.Message;
import com.chat.model.MessageType;
import com.chat.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MessageController messageController = new MessageController(messageService);
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
    }

    @Test
    void getRecentMessagesShouldUseDefaultLimitOfTen() throws Exception {
        Message message = Message.builder()
                .messageId("m1")
                .roomId("r1")
                .senderId("u1")
                .senderName("user")
                .content("hello")
                .type(MessageType.CHAT)
                .isRecalled(false)
                .build();
        message.setCreateTime(LocalDateTime.of(2026, 3, 5, 12, 0, 0));

        when(messageService.getRecentMessages("r1", 10)).thenReturn(List.of(message));

        mockMvc.perform(get("/api/messages/room/r1/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("r1"))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.messageCount").value(1))
                .andExpect(jsonPath("$.messages[0].messageId").value("m1"));

        verify(messageService).getRecentMessages("r1", 10);
    }
}
