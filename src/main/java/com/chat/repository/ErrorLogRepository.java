package com.chat.repository;

import com.chat.model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    List<ErrorLog> findByErrorCodeOrderByTimestampDesc(String errorCode);

    List<ErrorLog> findByErrorCategoryOrderByTimestampDesc(String errorCategory);

    List<ErrorLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    List<ErrorLog> findByUserIdOrderByTimestampDesc(String userId);

    List<ErrorLog> findByIpAddressOrderByTimestampDesc(String ipAddress);

    @Modifying
    @Transactional
    int deleteByTimestampBefore(LocalDateTime timestamp);
}