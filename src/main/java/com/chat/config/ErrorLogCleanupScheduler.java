package com.chat.config;

import com.chat.service.ErrorLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ErrorLogCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogCleanupScheduler.class);

    private final ErrorLogService errorLogService;

    @Autowired
    public ErrorLogCleanupScheduler(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldErrorLogs() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            log.info("开始清理30天前的错误日志，截止时间: {}", thirtyDaysAgo);
            errorLogService.deleteOldLogs(thirtyDaysAgo);
            log.info("错误日志清理完成");
        } catch (Exception e) {
            log.error("清理错误日志失败: {}", e.getMessage(), e);
        }
    }
}