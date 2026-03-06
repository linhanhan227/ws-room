package com.chat.controller;

import com.chat.annotation.AdminRequired;
import com.chat.model.Room;
import com.chat.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @AdminRequired
    public ResponseEntity<?> createRoom(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            String name = (String) body.get("name");
            String creator = adminUserId;
            String description = (String) body.get("description");
            Integer maxUsers = (Integer) body.get("maxUsers");
            Boolean isPrivate = (Boolean) body.get("isPrivate");
            String password = (String) body.get("password");

            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "房间名称不能为空"));
            }

            Room room = roomService.createRoom(name, creator, description, maxUsers, isPrivate, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "房间创建成功",
                    "roomId", room.getRoomId(),
                    "name", room.getName(),
                    "description", room.getDescription(),
                    "maxUsers", room.getMaxUsers(),
                    "isPrivate", room.getIsPrivate(),
                    "isActive", room.getIsActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable String roomId) {
        try {
            return roomService.getRoomById(roomId)
                    .map(room -> ResponseEntity.ok(Map.of(
                            "roomId", room.getRoomId(),
                            "name", room.getName(),
                            "creator", room.getCreator(),
                            "description", room.getDescription(),
                            "maxUsers", room.getMaxUsers(),
                            "isPrivate", room.getIsPrivate(),
                            "isActive", room.getIsActive(),
                            "createTime", room.getCreateTime()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(Map.of(
                    "roomCount", rooms.size(),
                    "rooms", rooms.stream().map(room -> Map.of(
                            "roomId", room.getRoomId(),
                            "name", room.getName(),
                            "creator", room.getCreator(),
                            "description", room.getDescription(),
                            "maxUsers", room.getMaxUsers(),
                            "isPrivate", room.getIsPrivate(),
                            "isActive", room.getIsActive(),
                            "userCount", roomService.getRoomUserCount(room.getRoomId())
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{roomId}")
    @AdminRequired
    public ResponseEntity<?> updateRoom(HttpServletRequest request, @PathVariable String roomId,
                                       @RequestBody Map<String, Object> body) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            String name = (String) body.get("name");
            String description = (String) body.get("description");
            Integer maxUsers = (Integer) body.get("maxUsers");

            Room room = roomService.updateRoom(roomId, name, description, maxUsers);
            return ResponseEntity.ok(Map.of(
                    "message", "房间更新成功",
                    "roomId", room.getRoomId(),
                    "name", room.getName(),
                    "description", room.getDescription(),
                    "maxUsers", room.getMaxUsers()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{roomId}")
    @AdminRequired
    public ResponseEntity<?> deleteRoom(HttpServletRequest request, @PathVariable String roomId) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            roomService.deleteRoom(roomId);
            return ResponseEntity.ok(Map.of(
                    "message", "房间删除成功",
                    "roomId", roomId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{roomId}/validate-password")
    public ResponseEntity<?> validatePassword(@PathVariable String roomId,
                                           @RequestBody Map<String, String> body) {
        try {
            String password = body.get("password");
            boolean isValid = roomService.validateRoomPassword(roomId, password);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "isValid", isValid
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{roomId}/password")
    @AdminRequired
    public ResponseEntity<?> updatePassword(HttpServletRequest request, @PathVariable String roomId,
                                           @RequestBody Map<String, String> body) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");

            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "新密码不能为空"));
            }

            Room room = roomService.updatePassword(roomId, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                    "message", "密码更新成功",
                    "roomId", room.getRoomId(),
                    "isPrivate", room.getIsPrivate()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{roomId}/privacy")
    @AdminRequired
    public ResponseEntity<?> updatePrivacy(HttpServletRequest request, @PathVariable String roomId,
                                          @RequestBody Map<String, Object> body) {
        try {
            String adminUserId = (String) request.getAttribute("userId");
            Boolean isPrivate = (Boolean) body.get("isPrivate");
            String password = (String) body.get("password");

            Room room = roomService.updatePrivacy(roomId, isPrivate, password);
            return ResponseEntity.ok(Map.of(
                    "message", "隐私设置更新成功",
                    "roomId", room.getRoomId(),
                    "isPrivate", room.getIsPrivate(),
                    "hasPassword", room.getPassword() != null && !room.getPassword().isEmpty()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
