package com.chat.service;

import com.chat.model.Message;
import com.chat.model.User;
import com.chat.repository.MessageRepository;
import com.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RoomService roomService;
    private final MessageService messageService;

    @Autowired
    public AdminService(UserRepository userRepository, RoomService roomService, MessageService messageService) {
        this.userRepository = userRepository;
        this.roomService = roomService;
        this.messageService = messageService;
    }

    @Transactional
    public User kickUser(String userId, String adminUserId) {
        if (!isAdmin(adminUserId)) {
            throw new RuntimeException("权限不足，只有管理员才能踢人");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setRoomId(null);
        user.setOnline(false);

        return userRepository.save(user);
    }

    @Transactional
    public User muteUser(String userId, String adminUserId, int minutes) {
        if (!isAdmin(adminUserId)) {
            throw new RuntimeException("权限不足，只有管理员才能禁言");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setMuted(true);
        user.setMutedUntil(LocalDateTime.now().plusMinutes(minutes));

        return userRepository.save(user);
    }

    @Transactional
    public User unmuteUser(String userId, String adminUserId) {
        if (!isAdmin(adminUserId)) {
            throw new RuntimeException("权限不足，只有管理员才能解除禁言");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setMuted(false);
        user.setMutedUntil(null);

        return userRepository.save(user);
    }

    @Transactional
    public User setAdmin(String userId, String adminUserId, boolean isAdmin) {
        if (!isAdmin(adminUserId)) {
            throw new RuntimeException("权限不足，只有管理员才能设置管理员权限");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setAdmin(isAdmin);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteRoom(String roomId, String adminUserId) {
        if (!isAdmin(adminUserId)) {
            throw new RuntimeException("权限不足，只有管理员才能删除房间");
        }

        List<User> users = userRepository.findByRoomId(roomId);
        for (User user : users) {
            user.setRoomId(null);
            user.setOnline(false);
            userRepository.save(user);
        }

        messageService.deleteMessagesByRoom(roomId);
        roomService.deleteRoom(roomId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getOnlineUsers() {
        return userRepository.findByIsOnlineTrue();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    public boolean isAdmin(String userId) {
        return userRepository.findByUserId(userId)
                .map(User::getAdmin)
                .orElse(false);
    }

    public long getTotalMessageCount() {
        return messageService.getMessagesBySender("").size();
    }
}
