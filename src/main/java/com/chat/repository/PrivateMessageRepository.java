package com.chat.repository;

import com.chat.model.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    Optional<PrivateMessage> findByMessageId(String messageId);

    List<PrivateMessage> findBySenderIdOrderByCreateTimeDesc(String senderId);

    List<PrivateMessage> findByReceiverIdOrderByCreateTimeDesc(String receiverId);

    List<PrivateMessage> findBySenderIdOrReceiverIdOrderByCreateTimeDesc(String senderId, String receiverId);

    List<PrivateMessage> findBySenderIdAndReceiverIdOrderByCreateTimeDesc(String senderId, String receiverId);

    List<PrivateMessage> findBySenderIdAndReceiverIdAndIsRecalledFalseOrderByCreateTimeDesc(String senderId, String receiverId);

    @Query("SELECT m FROM PrivateMessage m " +
            "WHERE m.isRecalled = false AND " +
            "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
            "ORDER BY m.createTime DESC")
    List<PrivateMessage> findConversationBothDirections(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Query("SELECT m FROM PrivateMessage m " +
            "WHERE m.isRecalled = false AND " +
            "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
            "ORDER BY m.createTime DESC")
    List<PrivateMessage> findConversationBothDirections(@Param("userId1") String userId1, @Param("userId2") String userId2, Pageable pageable);

    List<PrivateMessage> findByReceiverIdAndIsReadFalseOrderByCreateTimeDesc(String receiverId);

    List<PrivateMessage> findByReceiverIdAndIsReadFalse(String receiverId);

    void deleteByMessageId(String messageId);
}
