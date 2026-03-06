package com.chat.service;

import com.chat.model.Announcement;
import com.chat.repository.AnnouncementRepository;
import com.chat.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Autowired
    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional
    public Announcement createAnnouncement(String title, String content, String creatorId, String creatorName, Integer priority, Boolean isActive, LocalDateTime startTime, LocalDateTime endTime) {
        Announcement announcement = new Announcement.Builder()
                .announcementId(IdGenerator.generateNumericId())
                .title(title)
                .content(content)
                .creatorId(creatorId)
                .creatorName(creatorName)
                .priority(priority != null ? priority : 1)
                .isActive(isActive != null ? isActive : true)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return announcementRepository.save(announcement);
    }

    @Transactional
    public Optional<Announcement> updateAnnouncement(String announcementId, String title, String content, Integer priority, Boolean isActive, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<Announcement> announcementOpt = announcementRepository.findByAnnouncementId(announcementId);

        if (announcementOpt.isPresent()) {
            Announcement announcement = announcementOpt.get();

            if (title != null && !title.isEmpty()) {
                announcement.setTitle(title);
            }

            if (content != null && !content.isEmpty()) {
                announcement.setContent(content);
            }

            if (priority != null) {
                announcement.setPriority(priority);
            }

            if (isActive != null) {
                announcement.setIsActive(isActive);
            }

            if (startTime != null) {
                announcement.setStartTime(startTime);
            }

            if (endTime != null) {
                announcement.setEndTime(endTime);
            }

            return Optional.of(announcementRepository.save(announcement));
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<Announcement> deleteAnnouncement(String announcementId) {
        Optional<Announcement> announcementOpt = announcementRepository.findByAnnouncementId(announcementId);

        if (announcementOpt.isPresent()) {
            Announcement announcement = announcementOpt.get();
            announcementRepository.delete(announcement);
            return announcementOpt;
        }

        return Optional.empty();
    }

    public Optional<Announcement> getAnnouncementById(String announcementId) {
        return announcementRepository.findByAnnouncementId(announcementId);
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findByIsActiveTrueOrderByPriorityDescCreateTimeDesc();
    }

    public List<Announcement> getActiveAnnouncements() {
        LocalDateTime currentTime = LocalDateTime.now();
        return announcementRepository.findActiveAnnouncements(currentTime);
    }

    public List<Announcement> getActiveAnnouncementsByMinPriority(Integer minPriority) {
        LocalDateTime currentTime = LocalDateTime.now();
        return announcementRepository.findActiveAnnouncementsByMinPriority(minPriority, currentTime);
    }

    public List<Announcement> getAnnouncementsByCreator(String creatorId) {
        return announcementRepository.findByCreatorIdOrderByCreateTimeDesc(creatorId);
    }

    public List<Announcement> searchAnnouncements(String keyword) {
        return announcementRepository.searchAnnouncements(keyword);
    }

    @Transactional
    public Optional<Announcement> toggleAnnouncementStatus(String announcementId) {
        Optional<Announcement> announcementOpt = announcementRepository.findByAnnouncementId(announcementId);

        if (announcementOpt.isPresent()) {
            Announcement announcement = announcementOpt.get();
            announcement.setIsActive(!announcement.getIsActive());
            return Optional.of(announcementRepository.save(announcement));
        }

        return Optional.empty();
    }
}
