package com.chat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class SensitiveWordFileStorage {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordFileStorage.class);

    private static final String FILE_NAME = "sensitive_words.txt";
    private final String filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public SensitiveWordFileStorage() {
        this.filePath = getWorkingDirectory() + File.separator + FILE_NAME;
        initializeFile();
    }

    private String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    private void initializeFile() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                log.info("敏感词文件创建成功: {}", filePath);
                writeDefaultWords();
            } catch (IOException e) {
                log.error("创建敏感词文件失败: {}", e.getMessage());
                throw new RuntimeException("无法创建敏感词文件: " + e.getMessage(), e);
            }
        }
    }

    private void writeDefaultWords() {
        Set<String> defaultWords = new HashSet<>();
        defaultWords.add("敏感词");
        defaultWords.add("违禁词");
        defaultWords.add("广告");
        defaultWords.add("垃圾");
        defaultWords.add("骗子");
        writeWords(defaultWords);
        log.info("默认敏感词已写入文件");
    }

    public Set<String> readWords() {
        lock.readLock().lock();
        Set<String> words = new HashSet<>();
        
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("敏感词文件不存在，返回空集合");
                return words;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        words.add(line.toLowerCase());
                    }
                }
            }
            
            log.debug("从文件读取 {} 个敏感词", words.size());
            return words;
            
        } catch (IOException e) {
            log.error("读取敏感词文件失败: {}", e.getMessage());
            return words;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean writeWords(Set<String> words) {
        if (words == null) {
            return false;
        }

        lock.writeLock().lock();
        try {
            Path path = Paths.get(filePath);
            
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
                for (String word : words) {
                    if (word != null && !word.trim().isEmpty()) {
                        writer.write(word.trim().toLowerCase());
                        writer.newLine();
                    }
                }
                writer.flush();
            }
            
            log.info("成功写入 {} 个敏感词到文件", words.size());
            return true;
            
        } catch (IOException e) {
            log.error("写入敏感词文件失败: {}", e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean addWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        lock.writeLock().lock();
        try {
            Set<String> words = readWordsInternal();
            String normalizedWord = word.trim().toLowerCase();
            
            if (words.contains(normalizedWord)) {
                log.debug("敏感词已存在: {}", normalizedWord);
                return false;
            }
            
            words.add(normalizedWord);
            return writeWordsInternal(words);
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        lock.writeLock().lock();
        try {
            Set<String> words = readWordsInternal();
            String normalizedWord = word.trim().toLowerCase();
            
            if (!words.remove(normalizedWord)) {
                log.debug("敏感词不存在: {}", normalizedWord);
                return false;
            }
            
            return writeWordsInternal(words);
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean addWords(Set<String> newWords) {
        if (newWords == null || newWords.isEmpty()) {
            return false;
        }

        lock.writeLock().lock();
        try {
            Set<String> words = readWordsInternal();
            boolean added = false;
            
            for (String word : newWords) {
                if (word != null && !word.trim().isEmpty()) {
                    String normalizedWord = word.trim().toLowerCase();
                    if (!words.contains(normalizedWord)) {
                        words.add(normalizedWord);
                        added = true;
                    }
                }
            }
            
            if (added) {
                return writeWordsInternal(words);
            }
            return false;
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean containsWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        lock.readLock().lock();
        try {
            Set<String> words = readWordsInternal();
            return words.contains(word.trim().toLowerCase());
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getWordCount() {
        lock.readLock().lock();
        try {
            return readWordsInternal().size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    private Set<String> readWordsInternal() {
        Set<String> words = new HashSet<>();
        
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return words;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        words.add(line.toLowerCase());
                    }
                }
            }
            
            return words;
            
        } catch (IOException e) {
            log.error("读取敏感词文件失败: {}", e.getMessage());
            return words;
        }
    }

    private boolean writeWordsInternal(Set<String> words) {
        try {
            Path path = Paths.get(filePath);
            
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
                for (String word : words) {
                    if (word != null && !word.trim().isEmpty()) {
                        writer.write(word.trim().toLowerCase());
                        writer.newLine();
                    }
                }
                writer.flush();
            }
            
            log.debug("成功写入 {} 个敏感词", words.size());
            return true;
            
        } catch (IOException e) {
            log.error("写入敏感词文件失败: {}", e.getMessage());
            return false;
        }
    }
}
