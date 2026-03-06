package com.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "private_messages", indexes = {
    @Index(name = "idx_sender_id", columnList = "senderId"),
    @Index(name = "idx_receiver_id", columnList = "receiverId"),
    @Index(name = "idx_create_time", columnList = "createTime")
})
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String senderId;

    private String senderName;

    @Column(nullable = false)
    private String receiverId;

    private String receiverName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readTime;

    @Column(nullable = false)
    private Boolean isRecalled = false;

    private LocalDateTime recallTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    public PrivateMessage() {
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

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
        if (isRead == null) isRead = false;
        if (isRecalled == null) isRecalled = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PrivateMessage message = new PrivateMessage();

        public Builder messageId(String messageId) {
            message.setMessageId(messageId);
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

        public Builder receiverId(String receiverId) {
            message.setReceiverId(receiverId);
            return this;
        }

        public Builder receiverName(String receiverName) {
            message.setReceiverName(receiverName);
            return this;
        }

        public Builder content(String content) {
            message.setContent(content);
            return this;
        }

        public Builder isRead(Boolean isRead) {
            message.setIsRead(isRead);
            return this;
        }

        public Builder readTime(LocalDateTime readTime) {
            message.setReadTime(readTime);
            return this;
        }

        public Builder isRecalled(Boolean isRecalled) {
            message.setIsRecalled(isRecalled);
            return this;
        }

        public Builder recallTime(LocalDateTime recallTime) {
            message.setRecallTime(recallTime);
            return this;
        }

        public PrivateMessage build() {
            return message;
        }
    }
}
