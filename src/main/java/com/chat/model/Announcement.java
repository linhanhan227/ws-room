package com.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String announcementId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String creatorId;

    private String creatorName;

    @Column(nullable = false)
    private Integer priority = 1;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime updateTime;

    public Announcement() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (priority == null) priority = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Announcement announcement = new Announcement();

        public Builder announcementId(String announcementId) {
            announcement.setAnnouncementId(announcementId);
            return this;
        }

        public Builder title(String title) {
            announcement.setTitle(title);
            return this;
        }

        public Builder content(String content) {
            announcement.setContent(content);
            return this;
        }

        public Builder creatorId(String creatorId) {
            announcement.setCreatorId(creatorId);
            return this;
        }

        public Builder creatorName(String creatorName) {
            announcement.setCreatorName(creatorName);
            return this;
        }

        public Builder priority(Integer priority) {
            announcement.setPriority(priority);
            return this;
        }

        public Builder isActive(Boolean isActive) {
            announcement.setIsActive(isActive);
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            announcement.setStartTime(startTime);
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            announcement.setEndTime(endTime);
            return this;
        }

        public Announcement build() {
            return announcement;
        }
    }
}
