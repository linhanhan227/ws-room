package com.chat.repository;

import com.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByUsername(String username);
    List<User> findByRoomId(String roomId);
    List<User> findByIsOnlineTrue();
    boolean existsByUsername(String username);
    boolean existsByUserId(String userId);
}
