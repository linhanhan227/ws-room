package com.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String name;

    private String creator;

    private String description;

    @Column(nullable = false)
    private Integer maxUsers = 50;

    @Column(nullable = false)
    private Boolean isPrivate = false;

    private String password;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createTime;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        if (maxUsers == null) maxUsers = 50;
        if (isPrivate == null) isPrivate = false;
        if (isActive == null) isActive = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Room room = new Room();

        public Builder roomId(String roomId) {
            room.setRoomId(roomId);
            return this;
        }

        public Builder name(String name) {
            room.setName(name);
            return this;
        }

        public Builder creator(String creator) {
            room.setCreator(creator);
            return this;
        }

        public Builder description(String description) {
            room.setDescription(description);
            return this;
        }

        public Builder maxUsers(Integer maxUsers) {
            room.setMaxUsers(maxUsers);
            return this;
        }

        public Builder isPrivate(Boolean isPrivate) {
            room.setIsPrivate(isPrivate);
            return this;
        }

        public Builder password(String password) {
            room.setPassword(password);
            return this;
        }

        public Builder isActive(Boolean isActive) {
            room.setIsActive(isActive);
            return this;
        }

        public Room build() {
            return room;
        }
    }
}
