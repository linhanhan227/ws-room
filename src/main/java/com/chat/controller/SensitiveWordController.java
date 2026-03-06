package com.chat.controller;

import com.chat.annotation.AdminRequired;
import com.chat.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/sensitive-words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @Autowired
    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping
    @AdminRequired
    public ResponseEntity<Map<String, Object>> getAllSensitiveWords() {
        Set<String> words = sensitiveWordService.getAllSensitiveWords();
        Map<String, Object> response = new HashMap<>();
        response.put("count", words.size());
        response.put("words", words);
        response.put("filePath", sensitiveWordService.getFilePath());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @AdminRequired
    public ResponseEntity<Map<String, Object>> addSensitiveWord(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        if (word == null || word.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "敏感词不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        boolean added = sensitiveWordService.addSensitiveWord(word.trim());
        
        Map<String, Object> response = new HashMap<>();
        if (added) {
            response.put("message", "敏感词添加成功");
            response.put("word", word.trim());
            response.put("total", sensitiveWordService.getWordCount());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "敏感词已存在或添加失败");
            response.put("word", word.trim());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{word}")
    @AdminRequired
    public ResponseEntity<Map<String, Object>> removeSensitiveWord(@PathVariable String word) {
        boolean removed = sensitiveWordService.removeSensitiveWord(word);
        
        Map<String, Object> response = new HashMap<>();
        if (removed) {
            response.put("message", "敏感词删除成功");
            response.put("word", word);
            response.put("total", sensitiveWordService.getWordCount());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "敏感词不存在或删除失败");
            response.put("word", word);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/batch")
    @AdminRequired
    public ResponseEntity<Map<String, Object>> loadSensitiveWords(@RequestBody Map<String, Set<String>> request) {
        Set<String> words = request.get("words");
        if (words == null || words.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "敏感词列表不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        boolean saved = sensitiveWordService.loadSensitiveWords(words);
        
        Map<String, Object> response = new HashMap<>();
        if (saved) {
            response.put("message", "批量导入敏感词成功");
            response.put("imported", words.size());
            response.put("total", sensitiveWordService.getWordCount());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "批量导入敏感词失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文本不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        boolean contains = sensitiveWordService.containsSensitiveWord(text);
        List<String> detectedWords = sensitiveWordService.detectSensitiveWords(text);
        String filteredText = sensitiveWordService.filterText(text);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contains", contains);
        response.put("detectedWords", detectedWords);
        response.put("detectedCount", detectedWords.size());
        response.put("filteredText", filteredText);
        response.put("originalText", text);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterText(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        char replaceChar = '*';
        
        if (request.containsKey("replaceChar")) {
            String charStr = (String) request.get("replaceChar");
            if (charStr != null && !charStr.isEmpty()) {
                replaceChar = charStr.charAt(0);
            }
        }

        if (text == null || text.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文本不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        String filteredText = sensitiveWordService.filterText(text, replaceChar);
        
        Map<String, Object> response = new HashMap<>();
        response.put("filteredText", filteredText);
        response.put("originalText", text);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @AdminRequired
    public ResponseEntity<Map<String, Object>> getFileInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("filePath", sensitiveWordService.getFilePath());
        response.put("wordCount", sensitiveWordService.getWordCount());
        return ResponseEntity.ok(response);
    }
}
