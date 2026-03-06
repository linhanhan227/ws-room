package com.chat.service;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import com.chat.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, User> onlineUsers = new ConcurrentHashMap<>();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User.Builder()
                .userId(IdGenerator.generateNumericId())
                .username(username)
                .password(password)
                .isOnline(false)
                .isAdmin(false)
                .isMuted(false)
                .joinTime(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getUsersByRoom(String roomId) {
        return userRepository.findByRoomId(roomId);
    }

    public List<User> getOnlineUsers() {
        return userRepository.findByIsOnlineTrue();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User login(String username, String password, String sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getPassword() != null && !user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        user.setOnline(true);
        user.setSessionId(sessionId);
        user.setJoinTime(LocalDateTime.now());

        if (user.getMutedUntil() != null && user.getMutedUntil().isBefore(LocalDateTime.now())) {
            user.setMuted(false);
            user.setMutedUntil(null);
        }

        User savedUser = userRepository.save(user);
        onlineUsers.put(sessionId, savedUser);

        return savedUser;
    }

    @Transactional
    public void logout(String sessionId) {
        User user = onlineUsers.remove(sessionId);
        if (user != null) {
            user.setOnline(false);
            user.setSessionId(null);
            user.setRoomId(null);
            userRepository.save(user);
        }
    }

    public User getOnlineUser(String sessionId) {
        return onlineUsers.get(sessionId);
    }

    @Transactional
    public User updateUserRoom(String userId, String roomId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setRoomId(roomId);
        return userRepository.save(user);
    }

    @Transactional
    public User setAdmin(String userId, boolean isAdmin) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    @Transactional
    public User muteUser(String userId, LocalDateTime until) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setMuted(true);
        user.setMutedUntil(until);
        return userRepository.save(user);
    }

    @Transactional
    public User unmuteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setMuted(false);
        user.setMutedUntil(null);
        return userRepository.save(user);
    }

    public boolean isMuted(String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> {
                    if (user.getMutedUntil() != null && user.getMutedUntil().isBefore(LocalDateTime.now())) {
                        user.setMuted(false);
                        user.setMutedUntil(null);
                        userRepository.save(user);
                        return false;
                    }
                    return user.getMuted();
                })
                .orElse(false);
    }

    public boolean isAdmin(String userId) {
        return userRepository.findByUserId(userId)
                .map(User::getAdmin)
                .orElse(false);
    }

    public void updateSessionId(String userId, String sessionId) {
        userRepository.findByUserId(userId).ifPresent(user -> {
            user.setSessionId(sessionId);
            userRepository.save(user);
        });
    }

    @Transactional
    public User updateAvatar(String userId, String avatar) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    @Transactional
    public User updateSignature(String userId, String signature) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setSignature(signature);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String userId, String avatar, String signature) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (signature != null) {
            user.setSignature(signature);
        }
        return userRepository.save(user);
    }
}
