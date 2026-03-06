package com.chat.repository;

import com.chat.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomId(String roomId);
    List<Room> findByIsActiveTrue();
    boolean existsByRoomId(String roomId);
    boolean existsByName(String name);
}
