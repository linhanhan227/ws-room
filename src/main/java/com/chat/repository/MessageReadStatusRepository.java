package com.chat.repository;

import com.chat.model.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    Optional<MessageReadStatus> findByMessageIdAndUserId(String messageId, String userId);

    List<MessageReadStatus> findByMessageId(String messageId);

    List<MessageReadStatus> findByUserIdAndRoomId(String userId, String roomId);

    List<MessageReadStatus> findByUserId(String userId);

    List<MessageReadStatus> findByRoomId(String roomId);

    void deleteByMessageId(String messageId);

    void deleteByRoomId(String roomId);

    int countByMessageId(String messageId);
}
