package com.chat.repository;

import com.chat.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Optional<Announcement> findByAnnouncementId(String announcementId);

    List<Announcement> findByIsActiveTrueOrderByPriorityDescCreateTimeDesc();

    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.startTime IS NULL OR a.startTime <= :currentTime) AND (a.endTime IS NULL OR a.endTime >= :currentTime) ORDER BY a.priority DESC, a.createTime DESC")
    List<Announcement> findActiveAnnouncements(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND a.priority >= :minPriority AND (a.startTime IS NULL OR a.startTime <= :currentTime) AND (a.endTime IS NULL OR a.endTime >= :currentTime) ORDER BY a.priority DESC, a.createTime DESC")
    List<Announcement> findActiveAnnouncementsByMinPriority(@Param("minPriority") Integer minPriority, @Param("currentTime") LocalDateTime currentTime);

    List<Announcement> findByCreatorIdOrderByCreateTimeDesc(String creatorId);

    @Query("SELECT a FROM Announcement a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword% ORDER BY a.createTime DESC")
    List<Announcement> searchAnnouncements(@Param("keyword") String keyword);
}
