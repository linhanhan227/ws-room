package com.chat.service;

import com.chat.model.Room;
import com.chat.repository.RoomRepository;
import com.chat.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Room createRoom(String name, String creator, String description, Integer maxUsers, Boolean isPrivate, String password) {
        if (roomRepository.existsByName(name)) {
            throw new RuntimeException("房间名称已存在");
        }

        Room room = new Room.Builder()
                .roomId(IdGenerator.generateNumericId())
                .name(name)
                .creator(creator)
                .description(description)
                .maxUsers(maxUsers != null ? maxUsers : 50)
                .isPrivate(isPrivate != null ? isPrivate : false)
                .password(password)
                .isActive(true)
                .build();

        return roomRepository.save(room);
    }

    public Optional<Room> getRoomById(String roomId) {
        return roomRepository.findByRoomId(roomId);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findByIsActiveTrue();
    }

    @Transactional
    public Room updateRoom(String roomId, String name, String description, Integer maxUsers) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        if (name != null) {
            if (!name.equals(room.getName()) && roomRepository.existsByName(name)) {
                throw new RuntimeException("房间名称已存在");
            }
            room.setName(name);
        }
        if (description != null) {
            room.setDescription(description);
        }
        if (maxUsers != null) {
            room.setMaxUsers(maxUsers);
        }

        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(String roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        room.setIsActive(false);
        roomRepository.save(room);
    }

    @Transactional
    public void activateRoom(String roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        room.setIsActive(true);
        roomRepository.save(room);
    }

    public boolean isRoomPrivate(String roomId) {
        return roomRepository.findByRoomId(roomId)
                .map(Room::getIsPrivate)
                .orElse(false);
    }

    public boolean validateRoomPassword(String roomId, String password) {
        return roomRepository.findByRoomId(roomId)
                .map(room -> {
                    if (room.getIsPrivate()) {
                        return room.getPassword() != null && room.getPassword().equals(password);
                    }
                    return true;
                })
                .orElse(false);
    }

    public boolean existsByRoomId(String roomId) {
        return roomRepository.existsByRoomId(roomId);
    }

    public int getRoomUserCount(String roomId) {
        return 0;
    }

    @Transactional
    public Room updatePassword(String roomId, String oldPassword, String newPassword) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        if (!room.getIsPrivate()) {
            throw new RuntimeException("公开房间不能设置密码");
        }

        String currentPassword = room.getPassword();
        if (oldPassword != null && !oldPassword.isEmpty() && (currentPassword == null || !currentPassword.equals(oldPassword))) {
            throw new RuntimeException("旧密码错误");
        }

        room.setPassword(newPassword);
        return roomRepository.save(room);
    }

    @Transactional
    public Room updatePrivacy(String roomId, Boolean isPrivate, String password) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        if (isPrivate != null) {
            room.setIsPrivate(isPrivate);
        }

        if (isPrivate != null && isPrivate && password != null && !password.isEmpty()) {
            room.setPassword(password);
        } else if (isPrivate != null && !isPrivate) {
            room.setPassword(null);
        }

        return roomRepository.save(room);
    }
}
