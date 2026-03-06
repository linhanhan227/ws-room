package com.chat.repository;

import com.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdOrderByCreateTimeDesc(String roomId);
    List<Message> findBySenderIdOrderByCreateTimeDesc(String senderId);
    Optional<Message> findByMessageId(String messageId);
    List<Message> findByRoomIdAndIsRecalledFalseOrderByCreateTimeDesc(String roomId);

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.content LIKE %:keyword% AND m.isRecalled = false ORDER BY m.createTime DESC")
    List<Message> searchMessagesByRoom(@Param("roomId") String roomId, @Param("keyword") String keyword);

    @Query("SELECT m FROM Message m WHERE m.senderId = :senderId AND m.content LIKE %:keyword% AND m.isRecalled = false ORDER BY m.createTime DESC")
    List<Message> searchMessagesBySender(@Param("senderId") String senderId, @Param("keyword") String keyword);

    @Query("SELECT m FROM Message m WHERE m.content LIKE %:keyword% AND m.isRecalled = false ORDER BY m.createTime DESC")
    List<Message> searchAllMessages(@Param("keyword") String keyword);

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.senderId = :senderId AND m.content LIKE %:keyword% AND m.isRecalled = false ORDER BY m.createTime DESC")
    List<Message> searchMessagesByRoomAndSender(@Param("roomId") String roomId, @Param("senderId") String senderId, @Param("keyword") String keyword);
}
