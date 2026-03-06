package com.chat.controller;

import com.chat.annotation.AdminRequired;
import com.chat.model.User;
import com.chat.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/kick")
    @AdminRequired
    public ResponseEntity<?> kickUser(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            String userId = body.get("userId");
            String adminUserId = (String) request.getAttribute("userId");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户ID不能为空"));
            }

            User user = adminService.kickUser(userId, adminUserId);
            return ResponseEntity.ok(Map.of(
                    "message", "用户已被踢出",
                    "userId", user.getUserId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/mute")
    @AdminRequired
    public ResponseEntity<?> muteUser(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            String adminUserId = (String) request.getAttribute("userId");
            Integer minutes = (Integer) body.get("minutes");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户ID不能为空"));
            }
            if (minutes == null || minutes <= 0) {
                minutes = 30;
            }

            User user = adminService.muteUser(userId, adminUserId, minutes);
            return ResponseEntity.ok(Map.of(
                    "message", "用户已被禁言",
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "isMuted", user.getMuted(),
                    "mutedUntil", user.getMutedUntil()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/unmute")
    @AdminRequired
    public ResponseEntity<?> unmuteUser(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            String userId = body.get("userId");
            String adminUserId = (String) request.getAttribute("userId");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户ID不能为空"));
            }

            User user = adminService.unmuteUser(userId, adminUserId);
            return ResponseEntity.ok(Map.of(
                    "message", "用户禁言已解除",
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "isMuted", user.getMuted()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/set-admin")
    @AdminRequired
    public ResponseEntity<?> setAdmin(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            String adminUserId = (String) request.getAttribute("userId");
            Boolean isAdmin = (Boolean) body.get("isAdmin");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户ID不能为空"));
            }
            if (isAdmin == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "isAdmin 参数不能为空"));
            }

            User user = adminService.setAdmin(userId, adminUserId, isAdmin);
            return ResponseEntity.ok(Map.of(
                    "message", "管理员权限已设置",
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "isAdmin", user.getAdmin()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/room/{roomId}")
    @AdminRequired
    public ResponseEntity<?> deleteRoom(HttpServletRequest request, @PathVariable String roomId) {
        try {
            String adminUserId = (String) request.getAttribute("userId");

            adminService.deleteRoom(roomId, adminUserId);
            return ResponseEntity.ok(Map.of(
                    "message", "房间已删除",
                    "roomId", roomId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    @AdminRequired
    public ResponseEntity<?> getStats() {
        try {
            List<User> allUsers = adminService.getAllUsers();
            List<User> onlineUsers = adminService.getOnlineUsers();
            long totalMessages = adminService.getTotalMessageCount();

            return ResponseEntity.ok(Map.of(
                    "totalUsers", allUsers.size(),
                    "onlineUsers", onlineUsers.size(),
                    "totalMessages", totalMessages
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
