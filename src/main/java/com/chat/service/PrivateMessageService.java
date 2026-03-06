package com.chat.service;

import com.chat.model.PrivateMessage;
import com.chat.repository.PrivateMessageRepository;
import com.chat.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrivateMessageService {

    private final PrivateMessageRepository privateMessageRepository;

    @Autowired
    public PrivateMessageService(PrivateMessageRepository privateMessageRepository) {
        this.privateMessageRepository = privateMessageRepository;
    }

    @Transactional
    public PrivateMessage sendMessage(String senderId, String senderName, String receiverId, String receiverName, String content) {
        PrivateMessage message = new PrivateMessage.Builder()
                .messageId(IdGenerator.generateNumericId())
                .senderId(senderId)
                .senderName(senderName)
                .receiverId(receiverId)
                .receiverName(receiverName)
                .content(content)
                .isRead(false)
                .isRecalled(false)
                .build();

        return privateMessageRepository.save(message);
    }

    public Optional<PrivateMessage> getMessageById(String messageId) {
        return privateMessageRepository.findByMessageId(messageId);
    }

    public List<PrivateMessage> getConversation(String userId1, String userId2, int limit) {
        List<PrivateMessage> messages = privateMessageRepository.findConversationBothDirections(userId1, userId2);
        return messages.stream()
                .limit(limit)
                .toList();
    }

    public List<PrivateMessage> getConversation(String userId1, String userId2) {
        return privateMessageRepository.findConversationBothDirections(userId1, userId2);
    }

    public List<PrivateMessage> getRecentMessages(String userId, int limit) {
        List<PrivateMessage> messages = privateMessageRepository.findBySenderIdOrReceiverIdOrderByCreateTimeDesc(userId, userId);
        return messages.stream()
                .filter(m -> !m.getIsRecalled())
                .limit(limit)
                .toList();
    }

    public List<PrivateMessage> getUnreadMessages(String receiverId) {
        return privateMessageRepository.findByReceiverIdAndIsReadFalseOrderByCreateTimeDesc(receiverId);
    }

    @Transactional
    public Optional<PrivateMessage> markAsRead(String messageId) {
        Optional<PrivateMessage> messageOpt = privateMessageRepository.findByMessageId(messageId);

        if (messageOpt.isPresent()) {
            PrivateMessage message = messageOpt.get();

            if (message.getIsRead()) {
                return Optional.of(message);
            }

            message.setIsRead(true);
            message.setReadTime(LocalDateTime.now());

            return Optional.of(privateMessageRepository.save(message));
        }

        return Optional.empty();
    }

    @Transactional
    public void markAllAsRead(String receiverId) {
        List<PrivateMessage> unreadMessages = privateMessageRepository.findByReceiverIdAndIsReadFalse(receiverId);
        LocalDateTime now = LocalDateTime.now();

        for (PrivateMessage message : unreadMessages) {
            message.setIsRead(true);
            message.setReadTime(now);
            privateMessageRepository.save(message);
        }
    }

    @Transactional
    public Optional<PrivateMessage> recallMessage(String messageId, String userId) {
        Optional<PrivateMessage> messageOpt = privateMessageRepository.findByMessageId(messageId);

        if (messageOpt.isPresent()) {
            PrivateMessage message = messageOpt.get();

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

            return Optional.of(privateMessageRepository.save(message));
        }

        return Optional.empty();
    }

    @Transactional
    public void deleteMessage(String messageId) {
        privateMessageRepository.deleteByMessageId(messageId);
    }

    public int getUnreadCount(String receiverId) {
        return privateMessageRepository.findByReceiverIdAndIsReadFalse(receiverId).size();
    }
}
