package com.chat.controller;

import com.chat.annotation.AdminRequired;
import com.chat.model.Announcement;
import com.chat.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Autowired
    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    @AdminRequired
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String creatorId = (String) request.get("creatorId");
            String creatorName = (String) request.get("creatorName");
            Integer priority = request.get("priority") != null ? (Integer) request.get("priority") : 1;
            Boolean isActive = request.get("isActive") != null ? (Boolean) request.get("isActive") : true;
            
            LocalDateTime startTime = null;
            if (request.get("startTime") != null) {
                startTime = LocalDateTime.parse((String) request.get("startTime"));
            }
            
            LocalDateTime endTime = null;
            if (request.get("endTime") != null) {
                endTime = LocalDateTime.parse((String) request.get("endTime"));
            }

            if (title == null || title.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "标题不能为空"));
            }

            if (content == null || content.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "内容不能为空"));
            }

            if (creatorId == null || creatorId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "创建者ID不能为空"));
            }

            Announcement announcement = announcementService.createAnnouncement(
                    title, content, creatorId, creatorName, priority, isActive, startTime, endTime);

            return ResponseEntity.ok(Map.of(
                    "message", "公告创建成功",
                    "announcementId", announcement.getAnnouncementId(),
                    "title", announcement.getTitle(),
                    "content", announcement.getContent(),
                    "priority", announcement.getPriority(),
                    "isActive", announcement.getIsActive(),
                    "createTime", announcement.getCreateTime()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{announcementId}")
    public ResponseEntity<?> getAnnouncementById(@PathVariable String announcementId) {
        try {
            return announcementService.getAnnouncementById(announcementId)
                    .map(announcement -> {
                        Map<String, Object> result = new java.util.HashMap<>();
                        result.put("announcementId", announcement.getAnnouncementId());
                        result.put("title", announcement.getTitle());
                        result.put("content", announcement.getContent());
                        result.put("creatorId", announcement.getCreatorId());
                        result.put("creatorName", announcement.getCreatorName());
                        result.put("priority", announcement.getPriority());
                        result.put("isActive", announcement.getIsActive());
                        result.put("startTime", announcement.getStartTime());
                        result.put("endTime", announcement.getEndTime());
                        result.put("createTime", announcement.getCreateTime());
                        result.put("updateTime", announcement.getUpdateTime());
                        return ResponseEntity.ok(result);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("announcementCount", announcements.size());
            result.put("announcements", announcements.stream().map(announcement -> {
                Map<String, Object> a = new java.util.HashMap<>();
                a.put("announcementId", announcement.getAnnouncementId());
                a.put("title", announcement.getTitle());
                a.put("content", announcement.getContent());
                a.put("creatorId", announcement.getCreatorId());
                a.put("creatorName", announcement.getCreatorName());
                a.put("priority", announcement.getPriority());
                a.put("isActive", announcement.getIsActive());
                a.put("startTime", announcement.getStartTime());
                a.put("endTime", announcement.getEndTime());
                a.put("createTime", announcement.getCreateTime());
                a.put("updateTime", announcement.getUpdateTime());
                return a;
            }).toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getActiveAnnouncements();
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("announcementCount", announcements.size());
            result.put("announcements", announcements.stream().map(announcement -> {
                Map<String, Object> a = new java.util.HashMap<>();
                a.put("announcementId", announcement.getAnnouncementId());
                a.put("title", announcement.getTitle());
                a.put("content", announcement.getContent());
                a.put("creatorId", announcement.getCreatorId());
                a.put("creatorName", announcement.getCreatorName());
                a.put("priority", announcement.getPriority());
                a.put("startTime", announcement.getStartTime());
                a.put("endTime", announcement.getEndTime());
                a.put("createTime", announcement.getCreateTime());
                return a;
            }).toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/active/priority/{minPriority}")
    public ResponseEntity<?> getActiveAnnouncementsByMinPriority(@PathVariable Integer minPriority) {
        try {
            List<Announcement> announcements = announcementService.getActiveAnnouncementsByMinPriority(minPriority);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("minPriority", minPriority);
            result.put("announcementCount", announcements.size());
            result.put("announcements", announcements.stream().map(announcement -> {
                Map<String, Object> a = new java.util.HashMap<>();
                a.put("announcementId", announcement.getAnnouncementId());
                a.put("title", announcement.getTitle());
                a.put("content", announcement.getContent());
                a.put("creatorId", announcement.getCreatorId());
                a.put("creatorName", announcement.getCreatorName());
                a.put("priority", announcement.getPriority());
                a.put("startTime", announcement.getStartTime());
                a.put("endTime", announcement.getEndTime());
                a.put("createTime", announcement.getCreateTime());
                return a;
            }).toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<?> getAnnouncementsByCreator(@PathVariable String creatorId) {
        try {
            List<Announcement> announcements = announcementService.getAnnouncementsByCreator(creatorId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("creatorId", creatorId);
            result.put("announcementCount", announcements.size());
            result.put("announcements", announcements.stream().map(announcement -> {
                Map<String, Object> a = new java.util.HashMap<>();
                a.put("announcementId", announcement.getAnnouncementId());
                a.put("title", announcement.getTitle());
                a.put("content", announcement.getContent());
                a.put("priority", announcement.getPriority());
                a.put("isActive", announcement.getIsActive());
                a.put("startTime", announcement.getStartTime());
                a.put("endTime", announcement.getEndTime());
                a.put("createTime", announcement.getCreateTime());
                a.put("updateTime", announcement.getUpdateTime());
                return a;
            }).toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAnnouncements(@RequestParam String keyword) {
        try {
            List<Announcement> announcements = announcementService.searchAnnouncements(keyword);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("keyword", keyword);
            result.put("announcementCount", announcements.size());
            result.put("announcements", announcements.stream().map(announcement -> {
                Map<String, Object> a = new java.util.HashMap<>();
                a.put("announcementId", announcement.getAnnouncementId());
                a.put("title", announcement.getTitle());
                a.put("content", announcement.getContent());
                a.put("creatorId", announcement.getCreatorId());
                a.put("creatorName", announcement.getCreatorName());
                a.put("priority", announcement.getPriority());
                a.put("isActive", announcement.getIsActive());
                a.put("startTime", announcement.getStartTime());
                a.put("endTime", announcement.getEndTime());
                a.put("createTime", announcement.getCreateTime());
                return a;
            }).toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{announcementId}")
    @AdminRequired
    public ResponseEntity<?> updateAnnouncement(@PathVariable String announcementId,
                                                @RequestBody Map<String, Object> request) {
        try {
            String title = request.get("title") != null ? (String) request.get("title") : null;
            String content = request.get("content") != null ? (String) request.get("content") : null;
            Integer priority = request.get("priority") != null ? (Integer) request.get("priority") : null;
            Boolean isActive = request.get("isActive") != null ? (Boolean) request.get("isActive") : null;
            
            LocalDateTime startTime = null;
            if (request.get("startTime") != null) {
                startTime = LocalDateTime.parse((String) request.get("startTime"));
            }
            
            LocalDateTime endTime = null;
            if (request.get("endTime") != null) {
                endTime = LocalDateTime.parse((String) request.get("endTime"));
            }

            return announcementService.updateAnnouncement(
                    announcementId, title, content, priority, isActive, startTime, endTime)
                    .map(announcement -> {
                        Map<String, Object> result = new java.util.HashMap<>();
                        result.put("message", "公告更新成功");
                        result.put("announcementId", announcement.getAnnouncementId());
                        result.put("title", announcement.getTitle());
                        result.put("content", announcement.getContent());
                        result.put("priority", announcement.getPriority());
                        result.put("isActive", announcement.getIsActive());
                        result.put("startTime", announcement.getStartTime());
                        result.put("endTime", announcement.getEndTime());
                        result.put("updateTime", announcement.getUpdateTime());
                        return ResponseEntity.ok(result);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{announcementId}/toggle")
    @AdminRequired
    public ResponseEntity<?> toggleAnnouncementStatus(@PathVariable String announcementId) {
        try {
            return announcementService.toggleAnnouncementStatus(announcementId)
                    .map(announcement -> {
                        Map<String, Object> result = new java.util.HashMap<>();
                        result.put("message", "公告状态切换成功");
                        result.put("announcementId", announcement.getAnnouncementId());
                        result.put("isActive", announcement.getIsActive());
                        return ResponseEntity.ok(result);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{announcementId}")
    @AdminRequired
    public ResponseEntity<?> deleteAnnouncement(@PathVariable String announcementId) {
        try {
            return announcementService.deleteAnnouncement(announcementId)
                    .map(announcement -> {
                        Map<String, Object> result = new java.util.HashMap<>();
                        result.put("message", "公告删除成功");
                        result.put("announcementId", announcement.getAnnouncementId());
                        return ResponseEntity.ok(result);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
