package com.chat.controller;

import com.chat.model.Message;
import com.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<?> getMessageById(@PathVariable String messageId) {
        try {
            return messageService.getMessageById(messageId)
                    .map(message -> ResponseEntity.ok(toMessageDetailResponse(message)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getMessagesByRoom(@PathVariable String roomId) {
        try {
            List<Message> messages = messageService.getMessagesByRoom(roomId);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "messageCount", messages.size(),
                    "messages", messages.stream().map(this::toMessageSummaryResponse).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    @GetMapping("/room/{roomId}/recent")
    public ResponseEntity<?> getRecentMessages(@PathVariable String roomId,
                                             @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Message> messages = messageService.getRecentMessages(roomId, limit);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "limit", limit,
                    "messageCount", messages.size(),
                    "messages", messages.stream().map(this::toMessageSummaryResponse).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    @PutMapping("/{messageId}/recall")
    public ResponseEntity<?> recallMessage(@PathVariable String messageId,
                                          @RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户ID不能为空"));
            }

            return messageService.recallMessage(messageId, userId)
                    .map(message -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("message", "消息撤回成功");
                        response.put("messageId", message.getMessageId());
                        response.put("isRecalled", message.getIsRecalled());
                        response.put("recallTime", message.getRecallTime());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMessages(@RequestParam String keyword,
                                           @RequestParam(required = false) String roomId,
                                           @RequestParam(required = false) String senderId,
                                           @RequestParam(defaultValue = "50") int limit) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "搜索关键词不能为空"));
            }

            List<Message> messages;

            if (roomId != null && !roomId.isEmpty() && senderId != null && !senderId.isEmpty()) {
                messages = messageService.searchMessagesByRoomAndSender(roomId, senderId, keyword);
            } else if (roomId != null && !roomId.isEmpty()) {
                messages = messageService.searchMessagesByRoom(roomId, keyword);
            } else if (senderId != null && !senderId.isEmpty()) {
                messages = messageService.searchMessagesBySender(senderId, keyword);
            } else {
                messages = messageService.searchAllMessages(keyword);
            }

            List<Message> limitedMessages = messages.stream()
                    .limit(limit)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("keyword", keyword);
            response.put("roomId", roomId);
            response.put("senderId", senderId);
            response.put("totalCount", messages.size());
            response.put("returnedCount", limitedMessages.size());
            response.put("limit", limit);
            response.put("messages", limitedMessages.stream().map(this::toMessageWithRoomResponse).toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求处理失败"));
        }
    }

    private Map<String, Object> toMessageDetailResponse(Message message) {
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getMessageId());
        response.put("roomId", message.getRoomId());
        response.put("senderId", message.getSenderId());
        response.put("senderName", message.getSenderName());
        response.put("content", message.getContent());
        response.put("type", message.getType());
        response.put("isRecalled", message.getIsRecalled());
        response.put("recallTime", message.getRecallTime());
        response.put("createTime", message.getCreateTime());
        return response;
    }

    private Map<String, Object> toMessageSummaryResponse(Message message) {
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getMessageId());
        response.put("senderId", message.getSenderId());
        response.put("senderName", message.getSenderName());
        response.put("content", message.getContent());
        response.put("type", message.getType());
        response.put("createTime", message.getCreateTime());
        return response;
    }

    private Map<String, Object> toMessageWithRoomResponse(Message message) {
        Map<String, Object> response = toMessageSummaryResponse(message);
        response.put("roomId", message.getRoomId());
        return response;
    }
}
