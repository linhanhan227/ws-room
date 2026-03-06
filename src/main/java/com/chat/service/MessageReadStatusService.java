package com.chat.service;

import com.chat.model.MessageReadStatus;
import com.chat.repository.MessageReadStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageReadStatusService {

    private final MessageReadStatusRepository readStatusRepository;

    @Autowired
    public MessageReadStatusService(MessageReadStatusRepository readStatusRepository) {
        this.readStatusRepository = readStatusRepository;
    }

    @Transactional
    public MessageReadStatus markAsRead(String messageId, String userId, String roomId) {
        Optional<MessageReadStatus> existing = readStatusRepository.findByMessageIdAndUserId(messageId, userId);

        if (existing.isPresent()) {
            MessageReadStatus status = existing.get();
            if (!status.getIsRead()) {
                status.setIsRead(true);
                status.setReadTime(LocalDateTime.now());
                return readStatusRepository.save(status);
            }
            return status;
        }

        MessageReadStatus newStatus = new MessageReadStatus.Builder()
                .messageId(messageId)
                .userId(userId)
                .roomId(roomId)
                .isRead(true)
                .readTime(LocalDateTime.now())
                .build();

        return readStatusRepository.save(newStatus);
    }

    @Transactional
    public void markAllAsRead(String userId, String roomId) {
        List<MessageReadStatus> statuses = readStatusRepository.findByUserIdAndRoomId(userId, roomId);
        LocalDateTime now = LocalDateTime.now();

        for (MessageReadStatus status : statuses) {
            if (!status.getIsRead()) {
                status.setIsRead(true);
                status.setReadTime(now);
                readStatusRepository.save(status);
            }
        }
    }

    public Optional<MessageReadStatus> getReadStatus(String messageId, String userId) {
        return readStatusRepository.findByMessageIdAndUserId(messageId, userId);
    }

    public List<MessageReadStatus> getMessageReadStatuses(String messageId) {
        return readStatusRepository.findByMessageId(messageId);
    }

    public List<MessageReadStatus> getUserReadStatuses(String userId, String roomId) {
        return readStatusRepository.findByUserIdAndRoomId(userId, roomId);
    }

    public int getReadCount(String messageId) {
        return readStatusRepository.countByMessageId(messageId);
    }

    @Transactional
    public void deleteByMessage(String messageId) {
        readStatusRepository.deleteByMessageId(messageId);
    }

    @Transactional
    public void deleteByRoom(String roomId) {
        readStatusRepository.deleteByRoomId(roomId);
    }
}
