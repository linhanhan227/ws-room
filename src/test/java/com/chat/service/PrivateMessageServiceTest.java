package com.chat.service;

import com.chat.model.PrivateMessage;
import com.chat.repository.PrivateMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateMessageServiceTest {

    @Mock
    private PrivateMessageRepository privateMessageRepository;

    private PrivateMessageService privateMessageService;

    @BeforeEach
    void setUp() {
        privateMessageService = new PrivateMessageService(privateMessageRepository);
    }

    @Test
    void getConversationShouldIncludeBothDirections() {
        PrivateMessage aToB = PrivateMessage.builder()
                .senderId("u1")
                .receiverId("u2")
                .isRecalled(false)
                .build();
        PrivateMessage bToA = PrivateMessage.builder()
                .senderId("u2")
                .receiverId("u1")
                .isRecalled(false)
                .build();

        when(privateMessageRepository.findConversationBothDirections("u1", "u2"))
                .thenReturn(List.of(aToB, bToA));

        List<PrivateMessage> messages = privateMessageService.getConversation("u1", "u2");

        assertEquals(2, messages.size());
    }
}
