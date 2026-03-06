package com.chat.model;

public class UserInfo {
    private String userId;
    private String username;
    private Boolean isOnline;
    private Boolean isAdmin;
    private Boolean isMuted;
    private String avatar;
    private String signature;

    public UserInfo() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserInfo userInfo = new UserInfo();

        public Builder userId(String userId) {
            userInfo.setUserId(userId);
            return this;
        }

        public Builder username(String username) {
            userInfo.setUsername(username);
            return this;
        }

        public Builder isOnline(Boolean isOnline) {
            userInfo.setIsOnline(isOnline);
            return this;
        }

        public Builder isAdmin(Boolean isAdmin) {
            userInfo.setIsAdmin(isAdmin);
            return this;
        }

        public Builder isMuted(Boolean isMuted) {
            userInfo.setIsMuted(isMuted);
            return this;
        }

        public Builder avatar(String avatar) {
            userInfo.setAvatar(avatar);
            return this;
        }

        public Builder signature(String signature) {
            userInfo.setSignature(signature);
            return this;
        }

        public UserInfo build() {
            return userInfo;
        }
    }
}
