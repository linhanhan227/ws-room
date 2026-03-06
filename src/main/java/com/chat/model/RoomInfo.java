package com.chat.model;

public class RoomInfo {
    private String roomId;
    private String name;
    private String description;
    private Integer maxUsers;
    private Boolean isPrivate;
    private Integer userCount;

    public RoomInfo() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RoomInfo roomInfo = new RoomInfo();

        public Builder roomId(String roomId) {
            roomInfo.setRoomId(roomId);
            return this;
        }

        public Builder name(String name) {
            roomInfo.setName(name);
            return this;
        }

        public Builder description(String description) {
            roomInfo.setDescription(description);
            return this;
        }

        public Builder maxUsers(Integer maxUsers) {
            roomInfo.setMaxUsers(maxUsers);
            return this;
        }

        public Builder isPrivate(Boolean isPrivate) {
            roomInfo.setIsPrivate(isPrivate);
            return this;
        }

        public Builder userCount(Integer userCount) {
            roomInfo.setUserCount(userCount);
            return this;
        }

        public RoomInfo build() {
            return roomInfo;
        }
    }
}
