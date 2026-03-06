package com.chat.repository;

import com.chat.model.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<PrivateMessage> findByReceiverIdAndIsReadFalseOrderByCreateTimeDesc(String receiverId);

    List<PrivateMessage> findByReceiverIdAndIsReadFalse(String receiverId);

    void deleteByMessageId(String messageId);
}
