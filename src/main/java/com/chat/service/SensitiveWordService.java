package com.chat.service;

import com.chat.util.SensitiveWordFileStorage;
import com.chat.util.SensitiveWordFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class SensitiveWordService {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordService.class);

    private final SensitiveWordFilter filter;
    private final SensitiveWordFileStorage fileStorage;

    @Autowired
    public SensitiveWordService(SensitiveWordFilter filter, SensitiveWordFileStorage fileStorage) {
        this.filter = filter;
        this.fileStorage = fileStorage;
    }

    @PostConstruct
    public void init() {
        loadWordsFromFile();
        log.info("敏感词服务初始化完成，文件路径: {}", fileStorage.getFilePath());
    }

    private void loadWordsFromFile() {
        Set<String> words = fileStorage.readWords();
        if (!words.isEmpty()) {
            filter.loadSensitiveWords(words);
            log.info("从文件加载 {} 个敏感词", words.size());
        }
    }

    private void saveWordsToFile() {
        Set<String> words = filter.getSensitiveWords();
        fileStorage.writeWords(words);
    }

    public boolean containsSensitiveWord(String text) {
        return filter.containsSensitiveWord(text);
    }

    public String filterText(String text) {
        return filter.filter(text);
    }

    public String filterText(String text, char replaceChar) {
        return filter.filter(text, replaceChar);
    }

    public List<String> detectSensitiveWords(String text) {
        return filter.detectSensitiveWords(text);
    }

    public boolean addSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        String normalizedWord = word.trim().toLowerCase();
        
        if (filter.getSensitiveWords().contains(normalizedWord)) {
            log.debug("敏感词已存在: {}", normalizedWord);
            return false;
        }

        filter.addSensitiveWord(normalizedWord);
        
        boolean saved = fileStorage.addWord(normalizedWord);
        if (saved) {
            log.info("敏感词添加成功: {}", normalizedWord);
        } else {
            log.error("敏感词保存到文件失败: {}", normalizedWord);
        }
        
        return saved;
    }

    public boolean removeSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        String normalizedWord = word.trim().toLowerCase();
        
        if (!filter.getSensitiveWords().contains(normalizedWord)) {
            log.debug("敏感词不存在: {}", normalizedWord);
            return false;
        }

        filter.removeSensitiveWord(normalizedWord);
        
        boolean removed = fileStorage.removeWord(normalizedWord);
        if (removed) {
            log.info("敏感词删除成功: {}", normalizedWord);
        } else {
            log.error("敏感词从文件删除失败: {}", normalizedWord);
        }
        
        return removed;
    }

    public Set<String> getAllSensitiveWords() {
        return filter.getSensitiveWords();
    }

    public boolean loadSensitiveWords(Set<String> words) {
        if (words == null || words.isEmpty()) {
            return false;
        }

        Set<String> normalizedWords = new HashSet<>();
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                normalizedWords.add(word.trim().toLowerCase());
            }
        }

        filter.loadSensitiveWords(normalizedWords);
        
        boolean saved = fileStorage.writeWords(normalizedWords);
        if (saved) {
            log.info("批量导入敏感词成功，共 {} 个", normalizedWords.size());
        } else {
            log.error("批量导入敏感词保存到文件失败");
        }
        
        return saved;
    }

    public boolean kmpContains(String text, String pattern) {
        return filter.kmpContains(text, pattern);
    }

    public boolean trieContains(String text) {
        return filter.trieContains(text);
    }

    public List<String> trieDetect(String text) {
        return filter.trieDetect(text);
    }

    public void reloadFromFile() {
        loadWordsFromFile();
        log.info("敏感词已从文件重新加载");
    }

    public String getFilePath() {
        return fileStorage.getFilePath();
    }

    public int getWordCount() {
        return filter.getSensitiveWords().size();
    }
}
