package com.chat.service;

import com.chat.model.Message;
import com.chat.model.MessageType;
import com.chat.repository.MessageRepository;
import com.chat.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message saveMessage(String roomId, String senderId, String senderName, String content, MessageType type) {
        Message message = new Message.Builder()
                .messageId(IdGenerator.generateNumericId())
                .roomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .type(type)
                .isRecalled(false)
                .build();

        return messageRepository.save(message);
    }

    public List<Message> getMessagesByRoom(String roomId) {
        return messageRepository.findByRoomIdAndIsRecalledFalseOrderByCreateTimeDesc(roomId);
    }

    public List<Message> getRecentMessages(String roomId, int limit) {
        List<Message> messages = messageRepository.findByRoomIdOrderByCreateTimeDesc(roomId);
        return messages.stream()
                .filter(m -> !m.getIsRecalled())
                .limit(limit)
                .toList();
    }

    public Optional<Message> getMessageById(String messageId) {
        return messageRepository.findByMessageId(messageId);
    }

    @Transactional
    public Optional<Message> recallMessage(String messageId, String userId) {
        Optional<Message> messageOpt = messageRepository.findByMessageId(messageId);

        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();

            if (!message.getSenderId().equals(userId)) {
                throw new RuntimeException("只能撤回自己发送的消息");
            }

            if (message.getIsRecalled()) {
                throw new RuntimeException("消息已经被撤回");
            }

            LocalDateTime now = LocalDateTime.now();
            if (message.getCreateTime().plusMinutes(2).isBefore(now)) {
                throw new RuntimeException("消息已超过2分钟，无法撤回");
            }

            message.setIsRecalled(true);
            message.setRecallTime(now);
            message.setContent("该消息已被撤回");

            return Optional.of(messageRepository.save(message));
        }

        return Optional.empty();
    }

    public List<Message> getMessagesBySender(String senderId) {
        return messageRepository.findBySenderIdOrderByCreateTimeDesc(senderId);
    }

    @Transactional
    public void deleteMessagesByRoom(String roomId) {
        List<Message> messages = messageRepository.findByRoomIdOrderByCreateTimeDesc(roomId);
        messageRepository.deleteAll(messages);
    }

    public List<Message> searchMessagesByRoom(String roomId, String keyword) {
        return messageRepository.searchMessagesByRoom(roomId, keyword);
    }

    public List<Message> searchMessagesBySender(String senderId, String keyword) {
        return messageRepository.searchMessagesBySender(senderId, keyword);
    }

    public List<Message> searchAllMessages(String keyword) {
        return messageRepository.searchAllMessages(keyword);
    }

    public List<Message> searchMessagesByRoomAndSender(String roomId, String senderId, String keyword) {
        return messageRepository.searchMessagesByRoomAndSender(roomId, senderId, keyword);
    }
}
