package com.chat.config;

import com.chat.service.SensitiveWordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SensitiveWordScheduler {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordScheduler.class);

    private final SensitiveWordService sensitiveWordService;

    @Autowired
    public SensitiveWordScheduler(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @Scheduled(fixedRate = 60000)
    public void reloadSensitiveWords() {
        try {
            int oldCount = sensitiveWordService.getWordCount();
            sensitiveWordService.reloadFromFile();
            int newCount = sensitiveWordService.getWordCount();
            
            if (oldCount != newCount) {
                log.info("敏感词自动重载完成，词数从 {} 变更为 {}", oldCount, newCount);
            } else {
                log.debug("敏感词自动重载完成，词数无变化: {}", newCount);
            }
        } catch (Exception e) {
            log.error("敏感词自动重载失败: {}", e.getMessage(), e);
        }
    }
}
