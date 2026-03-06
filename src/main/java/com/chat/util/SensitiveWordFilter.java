package com.chat.util;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SensitiveWordFilter {

    private Set<String> sensitiveWords = new HashSet<>();
    private TrieNode trieRoot = new TrieNode();
    private ACNode acRoot = new ACNode();

    public void loadSensitiveWords(Set<String> words) {
        this.sensitiveWords = new HashSet<>(words);
        buildTrie();
        buildAC();
    }

    public void addSensitiveWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            sensitiveWords.add(word.trim().toLowerCase());
            buildTrie();
            buildAC();
        }
    }

    public void removeSensitiveWord(String word) {
        if (word != null) {
            sensitiveWords.remove(word.trim().toLowerCase());
            buildTrie();
            buildAC();
        }
    }

    public Set<String> getSensitiveWords() {
        return new HashSet<>(sensitiveWords);
    }

    public boolean containsSensitiveWord(String text) {
        return containsSensitiveWordAC(text);
    }

    public String filter(String text) {
        return filterAC(text, '*');
    }

    public String filter(String text, char replaceChar) {
        return filterAC(text, replaceChar);
    }

    public List<String> detectSensitiveWords(String text) {
        return detectSensitiveWordsAC(text);
    }

    public boolean kmpContains(String text, String pattern) {
        return kmpSearch(text.toLowerCase(), pattern.toLowerCase()) != -1;
    }

    private int kmpSearch(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) return -1;
        
        int[] lps = computeLPSArray(pattern);
        int i = 0, j = 0;
        
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    return i - j;
                }
            } else {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return -1;
    }

    private int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    public boolean trieContains(String text) {
        TrieNode current = trieRoot;
        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toLowerCase(text.charAt(i));
            if (!current.children.containsKey(ch)) {
                current = trieRoot;
            }
            if (current.children.containsKey(ch)) {
                current = current.children.get(ch);
                if (current.isEnd) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> trieDetect(String text) {
        List<String> detected = new ArrayList<>();
        for (String word : sensitiveWords) {
            if (trieSearchWord(text, word)) {
                detected.add(word);
            }
        }
        return detected;
    }

    private boolean trieSearchWord(String text, String word) {
        TrieNode current = trieRoot;
        String lowerText = text.toLowerCase();
        
        for (int i = 0; i < lowerText.length(); i++) {
            current = trieRoot;
            int j = i;
            while (j < lowerText.length()) {
                char ch = lowerText.charAt(j);
                if (current.children.containsKey(ch)) {
                    current = current.children.get(ch);
                    if (current.isEnd) {
                        return true;
                    }
                    j++;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private void buildTrie() {
        trieRoot = new TrieNode();
        for (String word : sensitiveWords) {
            TrieNode current = trieRoot;
            for (char ch : word.toLowerCase().toCharArray()) {
                current.children.putIfAbsent(ch, new TrieNode());
                current = current.children.get(ch);
            }
            current.isEnd = true;
        }
    }

    public boolean containsSensitiveWordAC(String text) {
        return !detectSensitiveWordsAC(text).isEmpty();
    }

    public String filterAC(String text, char replaceChar) {
        if (text == null || text.isEmpty()) return text;
        
        char[] chars = text.toCharArray();
        ACNode current = acRoot;
        
        for (int i = 0; i < chars.length; i++) {
            char ch = Character.toLowerCase(chars[i]);
            
            while (current != acRoot && !current.children.containsKey(ch)) {
                current = current.fail;
            }
            
            if (current.children.containsKey(ch)) {
                current = current.children.get(ch);
            }
            
            if (current.isEnd) {
                for (String word : current.output) {
                    int start = i - word.length() + 1;
                    for (int j = start; j <= i; j++) {
                        chars[j] = replaceChar;
                    }
                }
            }
        }
        
        return new String(chars);
    }

    public List<String> detectSensitiveWordsAC(String text) {
        List<String> detected = new ArrayList<>();
        if (text == null || text.isEmpty()) return detected;
        
        ACNode current = acRoot;
        
        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toLowerCase(text.charAt(i));
            
            while (current != acRoot && !current.children.containsKey(ch)) {
                current = current.fail;
            }
            
            if (current.children.containsKey(ch)) {
                current = current.children.get(ch);
            }
            
            if (current.isEnd) {
                detected.addAll(current.output);
            }
        }
        
        return new ArrayList<>(new LinkedHashSet<>(detected));
    }

    private void buildAC() {
        acRoot = new ACNode();
        
        for (String word : sensitiveWords) {
            ACNode current = acRoot;
            for (char ch : word.toLowerCase().toCharArray()) {
                current.children.putIfAbsent(ch, new ACNode());
                current = current.children.get(ch);
            }
            current.isEnd = true;
            current.output.add(word);
        }
        
        Queue<ACNode> queue = new LinkedList<>();
        
        for (ACNode node : acRoot.children.values()) {
            node.fail = acRoot;
            queue.offer(node);
        }
        
        while (!queue.isEmpty()) {
            ACNode current = queue.poll();
            
            for (Map.Entry<Character, ACNode> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                ACNode child = entry.getValue();
                
                ACNode failNode = current.fail;
                while (failNode != null && !failNode.children.containsKey(ch)) {
                    failNode = failNode.fail;
                }
                
                if (failNode == null) {
                    child.fail = acRoot;
                } else {
                    child.fail = failNode.children.get(ch);
                    if (child.fail.isEnd) {
                        child.isEnd = true;
                        child.output.addAll(child.fail.output);
                    }
                }
                
                queue.offer(child);
            }
        }
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }

    private static class ACNode {
        Map<Character, ACNode> children = new HashMap<>();
        ACNode fail;
        boolean isEnd = false;
        List<String> output = new ArrayList<>();
    }
}
