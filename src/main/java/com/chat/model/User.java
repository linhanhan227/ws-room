package com.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String roomId;

    private String sessionId;

    @Column(nullable = false)
    private Boolean isOnline = false;

    @Column(nullable = false)
    private Boolean isAdmin = false;

    @Column(nullable = false)
    private Boolean isMuted = false;

    @Column(nullable = false)
    private Long tokenVersion = 0L;

    private LocalDateTime mutedUntil;

    private LocalDateTime joinTime;

    private LocalDateTime createTime;

    private String avatar;

    private String signature;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public Boolean getMuted() {
        return isMuted;
    }

    public void setMuted(Boolean muted) {
        isMuted = muted;
    }

    public LocalDateTime getMutedUntil() {
        return mutedUntil;
    }

    public void setMutedUntil(LocalDateTime mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public Long getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(Long tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (isOnline == null) isOnline = false;
        if (isAdmin == null) isAdmin = false;
        if (isMuted == null) isMuted = false;
        if (tokenVersion == null) tokenVersion = 0L;
        if (joinTime == null) joinTime = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user = new User();

        public Builder userId(String userId) {
            user.setUserId(userId);
            return this;
        }

        public Builder username(String username) {
            user.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            user.setPassword(password);
            return this;
        }

        public Builder roomId(String roomId) {
            user.setRoomId(roomId);
            return this;
        }

        public Builder sessionId(String sessionId) {
            user.setSessionId(sessionId);
            return this;
        }

        public Builder isOnline(Boolean isOnline) {
            user.setOnline(isOnline);
            return this;
        }

        public Builder isAdmin(Boolean isAdmin) {
            user.setAdmin(isAdmin);
            return this;
        }

        public Builder isMuted(Boolean isMuted) {
            user.setMuted(isMuted);
            return this;
        }

        public Builder mutedUntil(LocalDateTime mutedUntil) {
            user.setMutedUntil(mutedUntil);
            return this;
        }

        public Builder tokenVersion(Long tokenVersion) {
            user.setTokenVersion(tokenVersion);
            return this;
        }

        public Builder joinTime(LocalDateTime joinTime) {
            user.setJoinTime(joinTime);
            return this;
        }

        public Builder avatar(String avatar) {
            user.setAvatar(avatar);
            return this;
        }

        public Builder signature(String signature) {
            user.setSignature(signature);
            return this;
        }

        public User build() {
            return user;
        }
    }
}
