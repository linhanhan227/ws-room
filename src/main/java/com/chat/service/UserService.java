package com.chat.service;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import com.chat.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserService {

    public static final List<String> DEFAULT_AVATARS = Arrays.asList(
            "http://api.tos.tiecode.org.cn/fbd3b2ceda35a3cf/%E5%B0%8F%E7%BA%A2%E4%B9%A6_12627_%E5%93%88%E5%93%88%E5%93%88%E5%93%88%E5%93%88%E6%80%8E%E4%B9%88%E8%B6%8A%E7%9C%8B%E8%B6%8A%E5%8F%AF%E7%88%B1__%E5%BD%93%E4%BD%A0%E9%9C%80%E8%A6%81%E4%B8%80%E5%BC%A0%E4%B8%AA%E6%80%A7%E5%8D%81%E8%B6%B3%E7%9A%84%E5%A4%B4%E5%83%8F%E6%97%B6%E8%BF%99%E6%AC%BE%E5%A4%B4%E5%83%8F%E6%80%BB%E8%83%BD%E6%BB%A1%E8%B6%B3%E4%BD%A0%E7%9A%84%E9%9C%80%E6%B1%82%E6%97%A0%E8%AE%BA%E6%98%AF%E5%A4%B8__8.jpg",
            "http://api.tos.tiecode.org.cn/84eec5035fc56f16/%E5%B0%8F%E7%BA%A2%E4%B9%A6_19285_%E4%B8%80%E8%BE%88%E5%AD%90%E4%B8%8D%E7%94%A8%E6%8D%A2%E7%9A%84%E5%BE%AE%E4%BF%A1%E5%A4%B4%E5%83%8F__%E6%83%B3%E7%9C%8B%E7%9C%8B%E4%BD%A0%E7%94%A8%E4%BA%86%E5%BE%88%E4%B9%85%E7%9A%84%E5%BE%AE%E4%BF%A1%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E5%B0%8F%E4%BC%97%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98_17.jpg",
            "http://api.tos.tiecode.org.cn/e59d4766d8a7a386/%E5%B0%8F%E7%BA%A2%E4%B9%A6_12627_%E5%93%88%E5%93%88%E5%93%88%E5%93%88%E5%93%88%E6%80%8E%E4%B9%88%E8%B6%8A%E7%9C%8B%E8%B6%8A%E5%8F%AF%E7%88%B1__%E5%BD%93%E4%BD%A0%E9%9C%80%E8%A6%81%E4%B8%80%E5%BC%A0%E4%B8%AA%E6%80%A7%E5%8D%81%E8%B6%B3%E7%9A%84%E5%A4%B4%E5%83%8F%E6%97%B6%E8%BF%99%E6%AC%BE%E5%A4%B4%E5%83%8F%E6%80%BB%E8%83%BD%E6%BB%A1%E8%B6%B3%E4%BD%A0%E7%9A%84%E9%9C%80%E6%B1%82%E6%97%A0%E8%AE%BA%E6%98%AF%E5%A4%B8__7.jpg",
            "http://api.tos.tiecode.org.cn/9c822e34a51bac3a/%E5%B0%8F%E7%BA%A2%E4%B9%A6_55442_%E6%B0%B8%E4%B9%85%E4%B8%8D%E6%8D%A2%E7%9A%84%E5%BE%AE%E4%BF%A1%E5%A4%B4%E5%83%8F__%E6%83%B3%E7%9C%8B%E7%9C%8B%E4%BD%A0%E7%94%A8%E4%BA%86%E5%BE%88%E4%B9%85%E7%9A%84%E5%BE%AE%E4%BF%A1%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E4%BC%98%E8%B4%A8%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E4%B8%AA%E6%80%A7%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E5%A4%B4%E5%83%8F%E6%8E%A8%E8%8D%90%E8%AF%9D%E9%A2%98__9.jpg",
            "http://api.tos.tiecode.org.cn/2af85fcbc028cd25/%E5%B0%8F%E7%BA%A2%E4%B9%A6_55442_%E6%B0%B8%E4%B9%85%E4%B8%8D%E6%8D%A2%E7%9A%84%E5%BE%AE%E4%BF%A1%E5%A4%B4%E5%83%8F__%E6%83%B3%E7%9C%8B%E7%9C%8B%E4%BD%A0%E7%94%A8%E4%BA%86%E5%BE%88%E4%B9%85%E7%9A%84%E5%BE%AE%E4%BF%A1%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E4%BC%98%E8%B4%A8%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E4%B8%AA%E6%80%A7%E5%A4%B4%E5%83%8F%E8%AF%9D%E9%A2%98%E5%A4%B4%E5%83%8F%E6%8E%A8%E8%8D%90%E8%AF%9D%E9%A2%98__3.jpg"
    );

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
                .avatar(randomDefaultAvatar())
                .build();

        return userRepository.save(user);
    }

    public String randomDefaultAvatar() {
        return DEFAULT_AVATARS.get(ThreadLocalRandom.current().nextInt(DEFAULT_AVATARS.size()));
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
