package com.chat.controller;

import com.chat.annotation.AdminRequired;
import com.chat.model.Room;
import com.chat.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
            Map<String, Object> response = new HashMap<>();
            response.put("message", "房间创建成功");
            response.put("roomId", room.getRoomId());
            response.put("name", room.getName());
            response.put("description", room.getDescription());
            response.put("maxUsers", room.getMaxUsers());
            response.put("isPrivate", room.getIsPrivate());
            response.put("isActive", room.getIsActive());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable String roomId) {
        try {
            return roomService.getRoomById(roomId)
                    .map(room -> ResponseEntity.ok(toRoomDetailResponse(room)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(Map.of(
                    "roomCount", rooms.size(),
                    "rooms", rooms.stream().map(this::toRoomListItemResponse).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
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
            Map<String, Object> response = new HashMap<>();
            response.put("message", "房间更新成功");
            response.put("roomId", room.getRoomId());
            response.put("name", room.getName());
            response.put("description", room.getDescription());
            response.put("maxUsers", room.getMaxUsers());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    private Map<String, Object> toRoomDetailResponse(Room room) {
        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getRoomId());
        response.put("name", room.getName());
        response.put("creator", room.getCreator());
        response.put("description", room.getDescription());
        response.put("maxUsers", room.getMaxUsers());
        response.put("isPrivate", room.getIsPrivate());
        response.put("isActive", room.getIsActive());
        response.put("createTime", room.getCreateTime());
        return response;
    }

    private Map<String, Object> toRoomListItemResponse(Room room) {
        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getRoomId());
        response.put("name", room.getName());
        response.put("creator", room.getCreator());
        response.put("description", room.getDescription());
        response.put("maxUsers", room.getMaxUsers());
        response.put("isPrivate", room.getIsPrivate());
        response.put("isActive", room.getIsActive());
        response.put("userCount", roomService.getRoomUserCount(room.getRoomId()));
        return response;
    }
}
