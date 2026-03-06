package com.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String senderId;

    private String senderName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(nullable = false)
    private Boolean isRecalled = false;

    private LocalDateTime recallTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    public Message() {
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Boolean getIsRecalled() {
        return isRecalled;
    }

    public void setIsRecalled(Boolean isRecalled) {
        this.isRecalled = isRecalled;
    }

    public LocalDateTime getRecallTime() {
        return recallTime;
    }

    public void setRecallTime(LocalDateTime recallTime) {
        this.recallTime = recallTime;
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
        if (isRecalled == null) isRecalled = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Message message = new Message();

        public Builder messageId(String messageId) {
            message.setMessageId(messageId);
            return this;
        }

        public Builder roomId(String roomId) {
            message.setRoomId(roomId);
            return this;
        }

        public Builder senderId(String senderId) {
            message.setSenderId(senderId);
            return this;
        }

        public Builder senderName(String senderName) {
            message.setSenderName(senderName);
            return this;
        }

        public Builder content(String content) {
            message.setContent(content);
            return this;
        }

        public Builder type(MessageType type) {
            message.setType(type);
            return this;
        }

        public Builder isRecalled(Boolean isRecalled) {
            message.setIsRecalled(isRecalled);
            return this;
        }

        public Message build() {
            return message;
        }
    }
}
