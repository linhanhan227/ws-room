package com.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_read_status", indexes = {
    @Index(name = "idx_message_id", columnList = "messageId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_room_id", columnList = "roomId")
})
public class MessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    public MessageReadStatus() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (isRead == null) isRead = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MessageReadStatus status = new MessageReadStatus();

        public Builder messageId(String messageId) {
            status.setMessageId(messageId);
            return this;
        }

        public Builder userId(String userId) {
            status.setUserId(userId);
            return this;
        }

        public Builder roomId(String roomId) {
            status.setRoomId(roomId);
            return this;
        }

        public Builder isRead(Boolean isRead) {
            status.setIsRead(isRead);
            return this;
        }

        public Builder readTime(LocalDateTime readTime) {
            status.setReadTime(readTime);
            return this;
        }

        public MessageReadStatus build() {
            return status;
        }
    }
}
