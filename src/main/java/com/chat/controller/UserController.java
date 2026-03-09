package com.chat.controller;

import com.chat.annotation.AdminRequired;
import com.chat.model.User;
import com.chat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                    "userCount", users.size(),
                    "users", users.stream().map(user -> Map.of(
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "isOnline", user.getOnline(),
                            "isAdmin", user.getAdmin(),
                            "isMuted", user.getMuted(),
                            "avatar", user.getAvatar(),
                            "signature", user.getSignature()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户名不能为空"));
            }

            User user = userService.createUser(username, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "用户注册成功",
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "avatar", user.getAvatar(),
                    "signature", user.getSignature()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            return userService.getUserById(userId)
                    .map(user -> ResponseEntity.ok(Map.of(
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "isOnline", user.getOnline(),
                            "isAdmin", user.getAdmin(),
                            "isMuted", user.getMuted(),
                            "roomId", user.getRoomId(),
                            "avatar", user.getAvatar(),
                            "signature", user.getSignature()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            return userService.getUserByUsername(username)
                    .map(user -> ResponseEntity.ok(Map.of(
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "isOnline", user.getOnline(),
                            "isAdmin", user.getAdmin(),
                            "isMuted", user.getMuted(),
                            "avatar", user.getAvatar(),
                            "signature", user.getSignature()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getUsersByRoom(@PathVariable String roomId) {
        try {
            List<User> users = userService.getUsersByRoom(roomId);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "userCount", users.size(),
                    "users", users.stream().map(user -> Map.of(
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "isOnline", user.getOnline(),
                            "isAdmin", user.getAdmin(),
                            "isMuted", user.getMuted(),
                            "avatar", user.getAvatar(),
                            "signature", user.getSignature()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/online")
    public ResponseEntity<?> getOnlineUsers() {
        try {
            List<User> users = userService.getOnlineUsers();
            return ResponseEntity.ok(Map.of(
                    "userCount", users.size(),
                    "users", users.stream().map(user -> Map.of(
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "isAdmin", user.getAdmin(),
                            "isMuted", user.getMuted(),
                            "avatar", user.getAvatar(),
                            "signature", user.getSignature()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/admin")
    @AdminRequired
    public ResponseEntity<?> setAdminStatus(HttpServletRequest request, @PathVariable String userId,
                                          @RequestBody Map<String, Boolean> body) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            Boolean isAdmin = body.get("isAdmin");
            if (isAdmin == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "isAdmin 参数不能为空"));
            }

            User user = userService.setAdmin(userId, isAdmin);
            return ResponseEntity.ok(Map.of(
                    "message", "管理员状态已更新",
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "isAdmin", user.getAdmin()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/mute")
    @AdminRequired
    public ResponseEntity<?> muteUser(HttpServletRequest request, @PathVariable String userId,
                                     @RequestBody Map<String, Integer> body) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            Integer minutes = body.get("minutes");
            if (minutes == null || minutes <= 0) {
                minutes = 30;
            }

            User user = userService.muteUser(userId, java.time.LocalDateTime.now().plusMinutes(minutes));
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

    @PutMapping("/{userId}/unmute")
    @AdminRequired
    public ResponseEntity<?> unmuteUser(HttpServletRequest request, @PathVariable String userId) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            User user = userService.unmuteUser(userId);
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

    @PutMapping("/{userId}/avatar")
    public ResponseEntity<?> updateAvatar(HttpServletRequest request, @PathVariable String userId,
                                           @RequestBody Map<String, String> body) {
        try {
            String currentUserId = (String) request.getAttribute("userId");
            if (currentUserId == null || !currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "只能修改自己的头像"));
            }

            String avatar = body.get("avatar");
            User user = userService.updateAvatar(userId, avatar);
            return ResponseEntity.ok(Map.of(
                    "message", "头像已更新",
                    "userId", user.getUserId(),
                    "avatar", user.getAvatar()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/signature")
    public ResponseEntity<?> updateSignature(HttpServletRequest request, @PathVariable String userId,
                                             @RequestBody Map<String, String> body) {
        try {
            String currentUserId = (String) request.getAttribute("userId");
            if (currentUserId == null || !currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "只能修改自己的个性签名"));
            }

            String signature = body.get("signature");
            User user = userService.updateSignature(userId, signature);
            return ResponseEntity.ok(Map.of(
                    "message", "个性签名已更新",
                    "userId", user.getUserId(),
                    "signature", user.getSignature()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @PathVariable String userId,
                                           @RequestBody Map<String, String> body) {
        try {
            String currentUserId = (String) request.getAttribute("userId");
            if (currentUserId == null || !currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "只能修改自己的资料"));
            }

            String avatar = body.get("avatar");
            String signature = body.get("signature");
            User user = userService.updateProfile(userId, avatar, signature);
            return ResponseEntity.ok(Map.of(
                    "message", "个人资料已更新",
                    "userId", user.getUserId(),
                    "avatar", user.getAvatar(),
                    "signature", user.getSignature()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
