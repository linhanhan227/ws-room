package com.chat.controller;

import com.chat.model.Message;
import com.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                    .map(message -> ResponseEntity.ok(Map.of(
                            "messageId", message.getMessageId(),
                            "roomId", message.getRoomId(),
                            "senderId", message.getSenderId(),
                            "senderName", message.getSenderName(),
                            "content", message.getContent(),
                            "type", message.getType(),
                            "isRecalled", message.getIsRecalled(),
                            "recallTime", message.getRecallTime(),
                            "createTime", message.getCreateTime()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getMessagesByRoom(@PathVariable String roomId) {
        try {
            List<Message> messages = messageService.getMessagesByRoom(roomId);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "messageCount", messages.size(),
                    "messages", messages.stream().map(message -> Map.of(
                            "messageId", message.getMessageId(),
                            "senderId", message.getSenderId(),
                            "senderName", message.getSenderName(),
                            "content", message.getContent(),
                            "type", message.getType(),
                            "createTime", message.getCreateTime()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomId}/recent")
    public ResponseEntity<?> getRecentMessages(@PathVariable String roomId,
                                             @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Message> messages = messageService.getRecentMessages(roomId, limit);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "limit", limit,
                    "messageCount", messages.size(),
                    "messages", messages.stream().map(message -> Map.of(
                            "messageId", message.getMessageId(),
                            "senderId", message.getSenderId(),
                            "senderName", message.getSenderName(),
                            "content", message.getContent(),
                            "type", message.getType(),
                            "createTime", message.getCreateTime()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
                    .map(message -> ResponseEntity.ok(Map.of(
                            "message", "消息撤回成功",
                            "messageId", message.getMessageId(),
                            "isRecalled", message.getIsRecalled(),
                            "recallTime", message.getRecallTime()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

            return ResponseEntity.ok(Map.of(
                    "keyword", keyword,
                    "roomId", roomId,
                    "senderId", senderId,
                    "totalCount", messages.size(),
                    "returnedCount", limitedMessages.size(),
                    "limit", limit,
                    "messages", limitedMessages.stream().map(message -> Map.of(
                            "messageId", message.getMessageId(),
                            "roomId", message.getRoomId(),
                            "senderId", message.getSenderId(),
                            "senderName", message.getSenderName(),
                            "content", message.getContent(),
                            "type", message.getType(),
                            "createTime", message.getCreateTime()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
