package com.chat.model;

import java.util.List;

public class ChatMessage {
    private MessageType type;
    private String roomId;
    private String sender;
    private String content;
    private String messageId;
    private Long timestamp;
    private Boolean isRecalled;
    private String targetUserId;
    private Integer muteMinutes;
    private Boolean isAdmin;
    private Boolean isPrivate;
    private String description;
    private Integer maxUsers;
    private String password;
    private List<RoomInfo> rooms;
    private List<UserInfo> users;
    private Boolean contains;
    private List<String> detectedWords;
    private String filteredText;
    private Integer readCount;
    private List<ChatMessage> searchResults;
    private String announcementId;
    private Integer priority;

    public ChatMessage() {
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIsRecalled() {
        return isRecalled;
    }

    public void setIsRecalled(Boolean isRecalled) {
        this.isRecalled = isRecalled;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Integer getMuteMinutes() {
        return muteMinutes;
    }

    public void setMuteMinutes(Integer muteMinutes) {
        this.muteMinutes = muteMinutes;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<RoomInfo> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomInfo> rooms) {
        this.rooms = rooms;
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    public Boolean getContains() {
        return contains;
    }

    public void setContains(Boolean contains) {
        this.contains = contains;
    }

    public List<String> getDetectedWords() {
        return detectedWords;
    }

    public void setDetectedWords(List<String> detectedWords) {
        this.detectedWords = detectedWords;
    }

    public String getFilteredText() {
        return filteredText;
    }

    public void setFilteredText(String filteredText) {
        this.filteredText = filteredText;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public List<ChatMessage> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<ChatMessage> searchResults) {
        this.searchResults = searchResults;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChatMessage message = new ChatMessage();

        public Builder type(MessageType type) {
            message.setType(type);
            return this;
        }

        public Builder roomId(String roomId) {
            message.setRoomId(roomId);
            return this;
        }

        public Builder sender(String sender) {
            message.setSender(sender);
            return this;
        }

        public Builder content(String content) {
            message.setContent(content);
            return this;
        }

        public Builder messageId(String messageId) {
            message.setMessageId(messageId);
            return this;
        }

        public Builder timestamp(Long timestamp) {
            message.setTimestamp(timestamp);
            return this;
        }

        public Builder isRecalled(Boolean isRecalled) {
            message.setIsRecalled(isRecalled);
            return this;
        }

        public Builder targetUserId(String targetUserId) {
            message.setTargetUserId(targetUserId);
            return this;
        }

        public Builder muteMinutes(Integer muteMinutes) {
            message.setMuteMinutes(muteMinutes);
            return this;
        }

        public Builder isAdmin(Boolean isAdmin) {
            message.setIsAdmin(isAdmin);
            return this;
        }

        public Builder isPrivate(Boolean isPrivate) {
            message.setIsPrivate(isPrivate);
            return this;
        }

        public Builder description(String description) {
            message.setDescription(description);
            return this;
        }

        public Builder maxUsers(Integer maxUsers) {
            message.setMaxUsers(maxUsers);
            return this;
        }

        public Builder password(String password) {
            message.setPassword(password);
            return this;
        }

        public Builder rooms(List<RoomInfo> rooms) {
            message.setRooms(rooms);
            return this;
        }

        public Builder users(List<UserInfo> users) {
            message.setUsers(users);
            return this;
        }

        public Builder contains(Boolean contains) {
            message.setContains(contains);
            return this;
        }

        public Builder detectedWords(List<String> detectedWords) {
            message.setDetectedWords(detectedWords);
            return this;
        }

        public Builder filteredText(String filteredText) {
            message.setFilteredText(filteredText);
            return this;
        }

        public Builder readCount(Integer readCount) {
            message.setReadCount(readCount);
            return this;
        }

        public Builder searchResults(List<ChatMessage> searchResults) {
            message.setSearchResults(searchResults);
            return this;
        }

        public Builder announcementId(String announcementId) {
            message.setAnnouncementId(announcementId);
            return this;
        }

        public Builder priority(Integer priority) {
            message.setPriority(priority);
            return this;
        }

        public ChatMessage build() {
            return message;
        }
    }
}
