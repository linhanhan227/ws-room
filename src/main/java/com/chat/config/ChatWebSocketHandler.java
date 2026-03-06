package com.chat.config;

import com.chat.model.*;
import com.chat.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final UserService userService;
    private final RoomService roomService;
    private final MessageService messageService;
    private final AdminService adminService;
    private final SensitiveWordService sensitiveWordService;
    private final MessageReadStatusService messageReadStatusService;
    private final PrivateMessageService privateMessageService;
    private final AnnouncementService announcementService;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUserId = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToRoomId = new ConcurrentHashMap<>();

    @Autowired
    public ChatWebSocketHandler(UserService userService, RoomService roomService,
                                MessageService messageService, AdminService adminService,
                                SensitiveWordService sensitiveWordService,
                                MessageReadStatusService messageReadStatusService,
                                PrivateMessageService privateMessageService,
                                AnnouncementService announcementService,
                                ObjectMapper objectMapper) {
        this.userService = userService;
        this.roomService = roomService;
        this.messageService = messageService;
        this.adminService = adminService;
        this.sensitiveWordService = sensitiveWordService;
        this.messageReadStatusService = messageReadStatusService;
        this.privateMessageService = privateMessageService;
        this.announcementService = announcementService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = getSessionId(session);
        String username = (String) session.getAttributes().get("username");
        String token = (String) session.getAttributes().get("token");
        Boolean authenticated = (Boolean) session.getAttributes().get("authenticated");

        sessions.put(sessionId, session);

        if (authenticated != null && authenticated && token != null) {
            String userId = (String) session.getAttributes().get("userId");

            try {
                User user = userService.getUserById(userId).orElse(null);
                if (user == null) {
                    user = userService.login(username, null, sessionId);
                } else {
                    user.setOnline(true);
                    user.setSessionId(sessionId);
                }
                sessionToUserId.put(sessionId, user.getUserId());

                ChatMessage welcome = new ChatMessage.Builder()
                        .type(MessageType.SYSTEM)
                        .content("欢迎 " + username + " 加入聊天室！（已认证）")
                        .timestamp(System.currentTimeMillis())
                        .build();
                sendMessage(session, welcome);

                List<Room> rooms = roomService.getAllRooms();
                sendMessage(session, buildRoomListMessage(rooms));

            } catch (Exception e) {
                ChatMessage error = new ChatMessage.Builder()
                        .type(MessageType.ERROR)
                        .content("认证失败: " + e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build();
                sendMessage(session, error);
            }
        } else {
            ChatMessage error = new ChatMessage.Builder()
                    .type(MessageType.ERROR)
                    .content("请提供Token")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, error);
        }

        log.info("WebSocket连接建立: {}, 当前在线: {} 人", sessionId, sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = getSessionId(session);
        String userId = sessionToUserId.get(sessionId);

        if (userId == null) {
            sendError(session, "请先登录");
            return;
        }

        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);

            switch (chatMessage.getType()) {
                case JOIN -> handleJoinRoom(session, sessionId, userId, chatMessage);
                case LEAVE -> handleLeaveRoom(session, sessionId, userId);
                case CHAT -> handleChatMessage(session, sessionId, userId, chatMessage);
                case CREATE_ROOM -> handleCreateRoom(session, userId, chatMessage);
                case DELETE_ROOM -> handleDeleteRoom(session, userId, chatMessage);
                case UPDATE_ROOM -> handleUpdateRoom(session, userId, chatMessage);
                case UPDATE_PASSWORD -> handleUpdatePassword(session, userId, chatMessage);
                case UPDATE_PRIVACY -> handleUpdatePrivacy(session, userId, chatMessage);
                case ROOM_LIST -> handleGetRoomList(session);
                case USER_LIST -> handleGetUserList(session, chatMessage.getRoomId());
                case KICK -> handleKickUser(session, userId, chatMessage);
                case MUTE -> handleMuteUser(session, userId, chatMessage);
                case UNMUTE -> handleUnmuteUser(session, userId, chatMessage);
                case RECALL -> handleRecallMessage(session, userId, chatMessage);
                case ADMIN -> handleAdminAction(session, userId, chatMessage);
                case CHECK_SENSITIVE -> handleCheckSensitive(session, chatMessage);
                case FILTER_SENSITIVE -> handleFilterSensitive(session, chatMessage);
                case MESSAGE_READ -> handleMessageRead(session, userId, chatMessage);
                case MESSAGE_READ_RECEIPT -> handleMessageReadReceipt(session, userId, chatMessage);
                case PRIVATE_MESSAGE -> handlePrivateMessage(session, sessionId, userId, chatMessage);
                case SEARCH_MESSAGES -> handleSearchMessages(session, userId, chatMessage);
                case ANNOUNCEMENT -> handleGetAnnouncement(session, chatMessage);
                case ANNOUNCEMENT_LIST -> handleGetAnnouncementList(session, chatMessage);
                case CREATE_ANNOUNCEMENT -> handleCreateAnnouncement(session, userId, chatMessage);
                case UPDATE_ANNOUNCEMENT -> handleUpdateAnnouncement(session, userId, chatMessage);
                case DELETE_ANNOUNCEMENT -> handleDeleteAnnouncement(session, userId, chatMessage);
                default -> sendError(session, "未知消息类型");
            }

        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
            sendError(session, "处理消息失败: " + e.getMessage());
        }
    }

    private void handleJoinRoom(WebSocketSession session, String sessionId, String userId, ChatMessage msg) throws IOException {
        String roomId = msg.getRoomId();
        if (roomId == null || roomId.isEmpty()) {
            sendError(session, "请指定房间ID");
            return;
        }

        Room room = roomService.getRoomById(roomId).orElse(null);
        if (room == null || !room.getIsActive()) {
            sendError(session, "房间不存在");
            return;
        }

        if (room.getIsPrivate()) {
            String password = msg.getContent();
            if (!roomService.validateRoomPassword(roomId, password)) {
                sendError(session, "房间密码错误");
                return;
            }
        }

        String currentRoomId = sessionToRoomId.get(sessionId);
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            handleLeaveRoom(session, sessionId, userId);
        }

        userService.updateUserRoom(userId, roomId);
        sessionToRoomId.put(sessionId, roomId);

        User user = userService.getUserById(userId).orElse(null);
        if (user != null) {
            messageService.saveMessage(roomId, userId, user.getUsername(),
                    user.getUsername() + " 加入了房间", MessageType.SYSTEM);

            ChatMessage systemMsg = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content(user.getUsername() + " 加入了房间")
                    .roomId(roomId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            broadcastToRoom(roomId, systemMsg, null);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.JOIN)
                    .content("成功加入房间: " + room.getName())
                    .roomId(roomId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

            List<Message> recentMessages = messageService.getRecentMessages(roomId, 10);
            for (Message m : recentMessages) {
                ChatMessage historyMsg = new ChatMessage.Builder()
                        .type(m.getType())
                        .content(m.getContent())
                        .sender(m.getSenderName())
                        .roomId(roomId)
                        .messageId(m.getMessageId())
                        .timestamp(m.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .isRecalled(m.getIsRecalled())
                        .build();
                sendMessage(session, historyMsg);
            }

            List<User> users = userService.getUsersByRoom(roomId);
            sendMessage(session, buildUserListMessage(roomId, users));
        }
    }

    private void handleLeaveRoom(WebSocketSession session, String sessionId, String userId) throws IOException {
        String roomId = sessionToRoomId.remove(sessionId);
        if (roomId == null) return;

        User user = userService.getUserById(userId).orElse(null);
        if (user != null) {
            userService.updateUserRoom(userId, null);

            messageService.saveMessage(roomId, userId, user.getUsername(),
                    user.getUsername() + " 离开了房间", MessageType.SYSTEM);

            ChatMessage systemMsg = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content(user.getUsername() + " 离开了房间")
                    .roomId(roomId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            broadcastToRoom(roomId, systemMsg, sessionId);

            List<User> users = userService.getUsersByRoom(roomId);
            broadcastToRoom(roomId, buildUserListMessage(roomId, users), null);
        }
    }

    private void handleChatMessage(WebSocketSession session, String sessionId, String userId, ChatMessage msg) throws IOException {
        String roomId = sessionToRoomId.get(sessionId);
        if (roomId == null) {
            sendError(session, "请先加入房间");
            return;
        }

        if (userService.isMuted(userId)) {
            sendError(session, "您已被禁言");
            return;
        }

        User user = userService.getUserById(userId).orElse(null);
        if (user == null) return;

        String content = msg.getContent();
        if (sensitiveWordService.containsSensitiveWord(content)) {
            List<String> detectedWords = sensitiveWordService.detectSensitiveWords(content);
            sendError(session, "消息包含敏感词: " + String.join(", ", detectedWords));
            return;
        }

        Message savedMessage = messageService.saveMessage(
                roomId, userId, user.getUsername(), content, MessageType.CHAT);

        ChatMessage chatMsg = new ChatMessage.Builder()
                .type(MessageType.CHAT)
                .content(content)
                .sender(user.getUsername())
                .roomId(roomId)
                .messageId(savedMessage.getMessageId())
                .timestamp(System.currentTimeMillis())
                .isRecalled(false)
                .build();

        broadcastToRoom(roomId, chatMsg, null);
    }

    private void handleCreateRoom(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(userId)) {
            sendError(session, "只有管理员才能创建房间");
            return;
        }

        String name = msg.getContent();
        if (name == null || name.isEmpty()) {
            sendError(session, "请提供房间名称");
            return;
        }

        try {
            User user = userService.getUserById(userId).orElse(null);
            String creator = user != null ? user.getUsername() : "系统";

            Room room = roomService.createRoom(name, creator, msg.getDescription(),
                    msg.getMaxUsers(), msg.getIsPrivate(), msg.getContent());

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("房间 [" + name + "] 创建成功")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

            handleGetRoomList(session);

        } catch (Exception e) {
            sendError(session, "创建房间失败: " + e.getMessage());
        }
    }

    private void handleDeleteRoom(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(userId)) {
            sendError(session, "只有管理员才能删除房间");
            return;
        }

        String roomId = msg.getRoomId();
        if (roomId == null) {
            sendError(session, "请指定房间ID");
            return;
        }

        try {
            roomService.deleteRoom(roomId);

            for (Map.Entry<String, String> entry : sessionToRoomId.entrySet()) {
                if (roomId.equals(entry.getValue())) {
                    String sessionId = entry.getKey();
                    String uid = sessionToUserId.get(sessionId);
                    if (uid != null) {
                        userService.updateUserRoom(uid, null);
                    }
                    sessionToRoomId.remove(sessionId);
                }
            }

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("房间已删除")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

            handleGetRoomList(session);

        } catch (Exception e) {
            sendError(session, "删除房间失败: " + e.getMessage());
        }
    }

    private void handleUpdateRoom(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(userId)) {
            sendError(session, "只有管理员才能更新房间");
            return;
        }

        String roomId = msg.getRoomId();
        if (roomId == null) {
            sendError(session, "请指定房间ID");
            return;
        }

        try {
            String name = msg.getContent();
            String description = msg.getDescription();
            Integer maxUsers = msg.getMaxUsers();

            Room room = roomService.updateRoom(roomId, name, description, maxUsers);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("房间更新成功")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

            handleGetRoomList(session);

        } catch (Exception e) {
            sendError(session, "更新房间失败: " + e.getMessage());
        }
    }

    private void handleUpdatePassword(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(userId)) {
            sendError(session, "只有管理员才能更新房间密码");
            return;
        }

        String roomId = msg.getRoomId();
        String oldPassword = msg.getPassword();
        String newPassword = msg.getContent();

        if (roomId == null) {
            sendError(session, "请指定房间ID");
            return;
        }

        if (newPassword == null || newPassword.isEmpty()) {
            sendError(session, "新密码不能为空");
            return;
        }

        try {
            roomService.updatePassword(roomId, oldPassword, newPassword);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("房间密码更新成功")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

        } catch (Exception e) {
            sendError(session, "更新房间密码失败: " + e.getMessage());
        }
    }

    private void handleUpdatePrivacy(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(userId)) {
            sendError(session, "只有管理员才能更新房间隐私设置");
            return;
        }

        String roomId = msg.getRoomId();
        Boolean isPrivate = msg.getIsPrivate();
        String password = msg.getPassword();

        if (roomId == null) {
            sendError(session, "请指定房间ID");
            return;
        }

        if (isPrivate == null) {
            sendError(session, "请指定是否为私有房间");
            return;
        }

        try {
            roomService.updatePrivacy(roomId, isPrivate, password);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("房间隐私设置更新成功")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

            handleGetRoomList(session);

        } catch (Exception e) {
            sendError(session, "更新房间隐私设置失败: " + e.getMessage());
        }
    }

    private void handleGetRoomList(WebSocketSession session) throws IOException {
        List<Room> rooms = roomService.getAllRooms();
        sendMessage(session, buildRoomListMessage(rooms));
    }

    private void handleGetUserList(WebSocketSession session, String roomId) throws IOException {
        if (roomId == null || roomId.isEmpty()) {
            List<User> onlineUsers = userService.getOnlineUsers();
            sendMessage(session, buildUserListMessage(null, onlineUsers));
        } else {
            List<User> users = userService.getUsersByRoom(roomId);
            sendMessage(session, buildUserListMessage(roomId, users));
        }
    }

    private void handleKickUser(WebSocketSession session, String adminUserId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(adminUserId)) {
            sendError(session, "权限不足");
            return;
        }

        String targetUserId = msg.getTargetUserId();
        if (targetUserId == null) {
            sendError(session, "请指定被踢用户");
            return;
        }

        try {
            User targetUser = adminService.kickUser(targetUserId, adminUserId);

            for (Map.Entry<String, String> entry : sessionToUserId.entrySet()) {
                if (targetUserId.equals(entry.getValue())) {
                    String sessionId = entry.getKey();
                    sessionToRoomId.remove(sessionId);

                    WebSocketSession targetSession = sessions.get(sessionId);
                    if (targetSession != null && targetSession.isOpen()) {
                        ChatMessage kickMsg = new ChatMessage.Builder()
                                .type(MessageType.SYSTEM)
                                .content("您已被管理员踢出房间")
                                .timestamp(System.currentTimeMillis())
                                .build();
                        sendMessage(targetSession, kickMsg);
                    }
                    break;
                }
            }

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("已将 " + targetUser.getUsername() + " 踢出房间")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

        } catch (Exception e) {
            sendError(session, "踢人失败: " + e.getMessage());
        }
    }

    private void handleMuteUser(WebSocketSession session, String adminUserId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(adminUserId)) {
            sendError(session, "权限不足");
            return;
        }

        String targetUserId = msg.getTargetUserId();
        int minutes = msg.getMuteMinutes() != null ? msg.getMuteMinutes() : 30;

        if (targetUserId == null) {
            sendError(session, "请指定被禁言用户");
            return;
        }

        try {
            User targetUser = adminService.muteUser(targetUserId, adminUserId, minutes);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("已将 " + targetUser.getUsername() + " 禁言 " + minutes + " 分钟")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

        } catch (Exception e) {
            sendError(session, "禁言失败: " + e.getMessage());
        }
    }

    private void handleUnmuteUser(WebSocketSession session, String adminUserId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(adminUserId)) {
            sendError(session, "权限不足");
            return;
        }

        String targetUserId = msg.getTargetUserId();
        if (targetUserId == null) {
            sendError(session, "请指定解除禁言用户");
            return;
        }

        try {
            User targetUser = adminService.unmuteUser(targetUserId, adminUserId);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content("已解除 " + targetUser.getUsername() + " 的禁言")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

        } catch (Exception e) {
            sendError(session, "解除禁言失败: " + e.getMessage());
        }
    }

    private void handleRecallMessage(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        String messageId = msg.getMessageId();
        if (messageId == null) {
            sendError(session, "请指定要撤回的消息ID");
            return;
        }

        try {
            Optional<Message> recalled = messageService.recallMessage(messageId, userId);

            if (recalled.isPresent()) {
                String roomId = sessionToRoomId.get(getSessionId(session));

                ChatMessage recallMsg = new ChatMessage.Builder()
                        .type(MessageType.RECALL)
                        .messageId(messageId)
                        .content("消息已撤回")
                        .roomId(roomId)
                        .timestamp(System.currentTimeMillis())
                        .build();

                if (roomId != null) {
                    broadcastToRoom(roomId, recallMsg, null);
                }
            }

        } catch (Exception e) {
            sendError(session, "撤回失败: " + e.getMessage());
        }
    }

    private void handleAdminAction(WebSocketSession session, String adminUserId, ChatMessage msg) throws IOException {
        if (!adminService.isAdmin(adminUserId)) {
            sendError(session, "权限不足");
            return;
        }

        String targetUserId = msg.getTargetUserId();
        boolean isAdmin = msg.getIsAdmin() != null && msg.getIsAdmin();

        try {
            User targetUser = adminService.setAdmin(targetUserId, adminUserId, isAdmin);

            ChatMessage success = new ChatMessage.Builder()
                    .type(MessageType.SYSTEM)
                    .content(targetUser.getUsername() + " 已成为管理员")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, success);

        } catch (Exception e) {
            sendError(session, "操作失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = getSessionId(session);
        String userId = sessionToUserId.remove(sessionId);
        String roomId = sessionToRoomId.remove(sessionId);

        sessions.remove(sessionId);

        if (userId != null) {
            userService.logout(sessionId);

            if (roomId != null) {
                User user = userService.getUserById(userId).orElse(null);
                if (user != null) {
                    messageService.saveMessage(roomId, userId, user.getUsername(),
                            user.getUsername() + " 离线了", MessageType.SYSTEM);

                    ChatMessage systemMsg = new ChatMessage.Builder()
                            .type(MessageType.SYSTEM)
                            .content(user.getUsername() + " 离线了")
                            .roomId(roomId)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    broadcastToRoom(roomId, systemMsg, sessionId);

                    List<User> users = userService.getUsersByRoom(roomId);
                    broadcastToRoom(roomId, buildUserListMessage(roomId, users), null);
                }
            }
        }

        log.info("WebSocket连接关闭: {}, 当前在线: {} 人", sessionId, sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    private void sendMessage(WebSocketSession session, ChatMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String content) throws IOException {
        ChatMessage error = new ChatMessage.Builder()
                .type(MessageType.ERROR)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        sendMessage(session, error);
    }

    private void broadcastToRoom(String roomId, ChatMessage message, String excludeSessionId) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("序列化消息失败: {}", e.getMessage());
            return;
        }

        for (Map.Entry<String, String> entry : sessionToRoomId.entrySet()) {
            if (roomId.equals(entry.getValue())) {
                String sessionId = entry.getKey();
                if (!sessionId.equals(excludeSessionId)) {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(json));
                        } catch (IOException e) {
                            log.error("广播消息失败: {}", e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void broadcastToAll(ChatMessage message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("序列化消息失败: {}", e.getMessage());
            return;
        }

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error("广播消息失败: {}", e.getMessage());
                }
            }
        }
    }

    private ChatMessage buildRoomListMessage(List<Room> rooms) {
        List<RoomInfo> roomInfos = rooms.stream()
                .map(r -> new RoomInfo.Builder()
                        .roomId(r.getRoomId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .maxUsers(r.getMaxUsers())
                        .isPrivate(r.getIsPrivate())
                        .userCount(userService.getUsersByRoom(r.getRoomId()).size())
                        .build())
                .collect(Collectors.toList());

        return new ChatMessage.Builder()
                .type(MessageType.ROOM_LIST)
                .rooms(roomInfos)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private ChatMessage buildUserListMessage(String roomId, List<User> users) {
        List<UserInfo> userInfos = users.stream()
                .map(u -> new UserInfo.Builder()
                        .userId(u.getUserId())
                        .username(u.getUsername())
                        .isOnline(u.getOnline())
                        .isAdmin(u.getAdmin())
                        .isMuted(u.getMuted())
                        .avatar(u.getAvatar())
                        .signature(u.getSignature())
                        .build())
                .collect(Collectors.toList());

        return new ChatMessage.Builder()
                .type(MessageType.USER_LIST)
                .roomId(roomId)
                .users(userInfos)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private void handleCheckSensitive(WebSocketSession session, ChatMessage msg) throws IOException {
        String text = msg.getContent();
        if (text == null || text.isEmpty()) {
            sendError(session, "文本不能为空");
            return;
        }

        boolean contains = sensitiveWordService.containsSensitiveWord(text);
        List<String> detectedWords = sensitiveWordService.detectSensitiveWords(text);
        String filteredText = sensitiveWordService.filterText(text);

        ChatMessage response = new ChatMessage.Builder()
                .type(MessageType.CHECK_SENSITIVE)
                .content(text)
                .contains(contains)
                .detectedWords(detectedWords)
                .filteredText(filteredText)
                .timestamp(System.currentTimeMillis())
                .build();
        sendMessage(session, response);
    }

    private void handleFilterSensitive(WebSocketSession session, ChatMessage msg) throws IOException {
        String text = msg.getContent();
        if (text == null || text.isEmpty()) {
            sendError(session, "文本不能为空");
            return;
        }

        String filteredText = sensitiveWordService.filterText(text);

        ChatMessage response = new ChatMessage.Builder()
                .type(MessageType.FILTER_SENSITIVE)
                .content(text)
                .filteredText(filteredText)
                .timestamp(System.currentTimeMillis())
                .build();
        sendMessage(session, response);
    }

    private void handleMessageRead(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        String messageId = msg.getMessageId();
        String roomId = msg.getRoomId();

        if (messageId == null || messageId.isEmpty()) {
            sendError(session, "请指定消息ID");
            return;
        }

        if (roomId == null || roomId.isEmpty()) {
            sendError(session, "请指定房间ID");
            return;
        }

        try {
            messageReadStatusService.markAsRead(messageId, userId, roomId);

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.MESSAGE_READ)
                    .messageId(messageId)
                    .content("消息已标记为已读")
                    .roomId(roomId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("标记消息已读失败: {}", e.getMessage(), e);
            sendError(session, "标记消息已读失败: " + e.getMessage());
        }
    }

    private void handleMessageReadReceipt(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        String messageId = msg.getMessageId();
        String roomId = msg.getRoomId();

        if (messageId == null || messageId.isEmpty()) {
            sendError(session, "请指定消息ID");
            return;
        }

        if (roomId == null || roomId.isEmpty()) {
            sendError(session, "请指定房间ID");
            return;
        }

        try {
            List<com.chat.model.MessageReadStatus> readStatuses = messageReadStatusService.getMessageReadStatuses(messageId);
            int readCount = readStatuses.size();

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.MESSAGE_READ_RECEIPT)
                    .messageId(messageId)
                    .content("消息已读回执")
                    .roomId(roomId)
                    .readCount(readCount)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String senderSessionId = null;
            for (Map.Entry<String, String> entry : sessionToUserId.entrySet()) {
                if (userId.equals(entry.getValue())) {
                    senderSessionId = entry.getKey();
                    break;
                }
            }

            if (senderSessionId != null) {
                WebSocketSession senderSession = sessions.get(senderSessionId);
                if (senderSession != null && senderSession.isOpen()) {
                    sendMessage(senderSession, response);
                }
            }

        } catch (Exception e) {
            log.error("发送消息已读回执失败: {}", e.getMessage(), e);
            sendError(session, "发送消息已读回执失败: " + e.getMessage());
        }
    }

    private void handlePrivateMessage(WebSocketSession session, String sessionId, String userId, ChatMessage msg) throws IOException {
        String receiverId = msg.getTargetUserId();
        String content = msg.getContent();

        if (receiverId == null || receiverId.isEmpty()) {
            sendError(session, "请指定接收者ID");
            return;
        }

        if (content == null || content.isEmpty()) {
            sendError(session, "消息内容不能为空");
            return;
        }

        if (userService.isMuted(userId)) {
            sendError(session, "您已被禁言");
            return;
        }

        User sender = userService.getUserById(userId).orElse(null);
        User receiver = userService.getUserById(receiverId).orElse(null);

        if (sender == null) {
            sendError(session, "发送者不存在");
            return;
        }

        if (receiver == null) {
            sendError(session, "接收者不存在");
            return;
        }

        if (sensitiveWordService.containsSensitiveWord(content)) {
            List<String> detectedWords = sensitiveWordService.detectSensitiveWords(content);
            sendError(session, "消息包含敏感词: " + String.join(", ", detectedWords));
            return;
        }

        try {
            PrivateMessage savedMessage = privateMessageService.sendMessage(
                    userId, sender.getUsername(), receiverId, receiver.getUsername(), content);

            ChatMessage privateMsg = new ChatMessage.Builder()
                    .type(MessageType.PRIVATE_MESSAGE)
                    .content(content)
                    .sender(sender.getUsername())
                    .targetUserId(receiverId)
                    .messageId(savedMessage.getMessageId())
                    .timestamp(System.currentTimeMillis())
                    .isRecalled(false)
                    .build();

            for (Map.Entry<String, String> entry : sessionToUserId.entrySet()) {
                String targetSessionId = entry.getKey();
                String targetUserId = entry.getValue();

                if (userId.equals(targetUserId) || receiverId.equals(targetUserId)) {
                    WebSocketSession targetSession = sessions.get(targetSessionId);
                    if (targetSession != null && targetSession.isOpen()) {
                        sendMessage(targetSession, privateMsg);
                    }
                }
            }

        } catch (Exception e) {
            log.error("发送私聊消息失败: {}", e.getMessage(), e);
            sendError(session, "发送私聊消息失败: " + e.getMessage());
        }
    }

    private void handleSearchMessages(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        String keyword = msg.getContent();
        String roomId = msg.getRoomId();
        String senderId = msg.getSender();
        Integer limit = msg.getMaxUsers() != null ? msg.getMaxUsers() : 50;

        if (keyword == null || keyword.isEmpty()) {
            sendError(session, "搜索关键词不能为空");
            return;
        }

        try {
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

            List<ChatMessage> searchResults = limitedMessages.stream()
                    .map(m -> new ChatMessage.Builder()
                            .type(m.getType())
                            .content(m.getContent())
                            .sender(m.getSenderName())
                            .roomId(m.getRoomId())
                            .messageId(m.getMessageId())
                            .timestamp(m.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                            .isRecalled(m.getIsRecalled())
                            .build())
                    .toList();

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.SEARCH_MESSAGES)
                    .content("搜索结果")
                    .roomId(roomId)
                    .sender(senderId)
                    .searchResults(searchResults)
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, response);

        } catch (Exception e) {
            log.error("搜索消息失败: {}", e.getMessage(), e);
            sendError(session, "搜索消息失败: " + e.getMessage());
        }
    }

    private void handleGetAnnouncement(WebSocketSession session, ChatMessage msg) throws IOException {
        String announcementId = msg.getAnnouncementId();

        if (announcementId == null || announcementId.isEmpty()) {
            sendError(session, "请指定公告ID");
            return;
        }

        try {
            Announcement announcement = announcementService.getAnnouncementById(announcementId).orElse(null);

            if (announcement == null) {
                sendError(session, "公告不存在");
                return;
            }

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.ANNOUNCEMENT)
                    .content(announcement.getTitle())
                    .description(announcement.getContent())
                    .sender(announcement.getCreatorName())
                    .messageId(announcement.getAnnouncementId())
                    .isAdmin(announcement.getIsActive())
                    .timestamp(announcement.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

            sendMessage(session, response);

        } catch (Exception e) {
            log.error("获取公告失败: {}", e.getMessage(), e);
            sendError(session, "获取公告失败: " + e.getMessage());
        }
    }

    private void handleGetAnnouncementList(WebSocketSession session, ChatMessage msg) throws IOException {
        try {
            List<Announcement> announcements;
            Integer minPriority = msg.getPriority();

            if (minPriority != null && minPriority > 0) {
                announcements = announcementService.getActiveAnnouncementsByMinPriority(minPriority);
            } else {
                announcements = announcementService.getActiveAnnouncements();
            }

            List<ChatMessage> announcementList = announcements.stream()
                    .map(a -> new ChatMessage.Builder()
                            .type(MessageType.ANNOUNCEMENT)
                            .content(a.getTitle())
                            .description(a.getContent())
                            .sender(a.getCreatorName())
                            .messageId(a.getAnnouncementId())
                            .isAdmin(a.getIsActive())
                            .timestamp(a.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                            .build())
                    .toList();

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.ANNOUNCEMENT_LIST)
                    .content("公告列表")
                    .searchResults(announcementList)
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, response);

        } catch (Exception e) {
            log.error("获取公告列表失败: {}", e.getMessage(), e);
            sendError(session, "获取公告列表失败: " + e.getMessage());
        }
    }

    private void handleCreateAnnouncement(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                sendError(session, "用户不存在");
                return;
            }

            if (!user.getIsAdmin()) {
                sendError(session, "只有管理员可以创建公告");
                return;
            }

            String title = msg.getContent();
            String content = msg.getDescription();
            Integer priority = msg.getPriority() != null ? msg.getPriority() : 1;

            if (title == null || title.isEmpty()) {
                sendError(session, "标题不能为空");
                return;
            }

            if (content == null || content.isEmpty()) {
                sendError(session, "内容不能为空");
                return;
            }

            Announcement announcement = announcementService.createAnnouncement(
                    title, content, userId, user.getUsername(), priority, true, null, null);

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.CREATE_ANNOUNCEMENT)
                    .content("公告创建成功")
                    .messageId(announcement.getAnnouncementId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, response);

            ChatMessage broadcastMsg = new ChatMessage.Builder()
                    .type(MessageType.ANNOUNCEMENT)
                    .content(announcement.getTitle())
                    .description(announcement.getContent())
                    .sender(announcement.getCreatorName())
                    .messageId(announcement.getAnnouncementId())
                    .isAdmin(announcement.getIsActive())
                    .timestamp(announcement.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

            broadcastToAll(broadcastMsg);

        } catch (Exception e) {
            log.error("创建公告失败: {}", e.getMessage(), e);
            sendError(session, "创建公告失败: " + e.getMessage());
        }
    }

    private void handleUpdateAnnouncement(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                sendError(session, "用户不存在");
                return;
            }

            if (!user.getIsAdmin()) {
                sendError(session, "只有管理员可以更新公告");
                return;
            }

            String announcementId = msg.getAnnouncementId();
            if (announcementId == null || announcementId.isEmpty()) {
                sendError(session, "请指定公告ID");
                return;
            }

            String title = msg.getContent();
            String content = msg.getDescription();
            Integer priority = msg.getPriority();
            Boolean isActive = msg.getIsAdmin();

            Announcement announcement = announcementService.updateAnnouncement(
                    announcementId, title, content, priority, isActive, null, null).orElse(null);

            if (announcement == null) {
                sendError(session, "公告不存在");
                return;
            }

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.UPDATE_ANNOUNCEMENT)
                    .content("公告更新成功")
                    .messageId(announcement.getAnnouncementId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, response);

        } catch (Exception e) {
            log.error("更新公告失败: {}", e.getMessage(), e);
            sendError(session, "更新公告失败: " + e.getMessage());
        }
    }

    private void handleDeleteAnnouncement(WebSocketSession session, String userId, ChatMessage msg) throws IOException {
        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                sendError(session, "用户不存在");
                return;
            }

            if (!user.getIsAdmin()) {
                sendError(session, "只有管理员可以删除公告");
                return;
            }

            String announcementId = msg.getAnnouncementId();
            if (announcementId == null || announcementId.isEmpty()) {
                sendError(session, "请指定公告ID");
                return;
            }

            Announcement announcement = announcementService.deleteAnnouncement(announcementId).orElse(null);

            if (announcement == null) {
                sendError(session, "公告不存在");
                return;
            }

            ChatMessage response = new ChatMessage.Builder()
                    .type(MessageType.DELETE_ANNOUNCEMENT)
                    .content("公告删除成功")
                    .messageId(announcement.getAnnouncementId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, response);

        } catch (Exception e) {
            log.error("删除公告失败: {}", e.getMessage(), e);
            sendError(session, "删除公告失败: " + e.getMessage());
        }
    }

    private String getSessionId(WebSocketSession session) {
        return (String) session.getAttributes().get("sessionId");
    }
}
