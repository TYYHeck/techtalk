package com.techtalk.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 敏感词过滤器（DFA 算法）
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    /** 敏感词库（可扩展） */
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            // 这里放置敏感词，生产环境应从数据库或文件加载
            "fuck", "shit", "sb", "傻逼", "操你", "妈的"
    ));

    private final Map<Character, Object> dfaMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (String word : SENSITIVE_WORDS) {
            Map<Character, Object> currentMap = dfaMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                @SuppressWarnings("unchecked")
                Map<Character, Object> subMap = (Map<Character, Object>) currentMap.get(c);
                if (subMap == null) {
                    subMap = new HashMap<>();
                    currentMap.put(c, subMap);
                }
                currentMap = subMap;
            }
            currentMap.put('*', Boolean.TRUE); // 结束标记
        }
        log.info("敏感词过滤器初始化完成，共 {} 个词", SENSITIVE_WORDS.size());
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return false;
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            int matchLen = checkSensitiveWord(text, i);
            if (matchLen > 0) return true;
        }
        return false;
    }

    /**
     * 替换敏感词为 ***
     */
    public String replace(String text) {
        if (text == null || text.isEmpty()) return text;
        String lowerText = text.toLowerCase();
        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < lowerText.length(); i++) {
            int matchLen = checkSensitiveWord(lowerText, i);
            if (matchLen > 0) {
                for (int j = i; j < i + matchLen; j++) {
                    result.setCharAt(j, '*');
                }
                i += matchLen - 1;
            }
        }
        return result.toString();
    }

    private int checkSensitiveWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = dfaMap;
        int matchLen = 0;
        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            @SuppressWarnings("unchecked")
            Map<Character, Object> subMap = (Map<Character, Object>) currentMap.get(c);
            if (subMap == null) break;
            matchLen++;
            if (subMap.containsKey('*')) {
                return matchLen;
            }
            currentMap = subMap;
        }
        return 0;
    }
}
