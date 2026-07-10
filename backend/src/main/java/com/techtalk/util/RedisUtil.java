package com.techtalk.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ============ 通用操作 ============

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 安全地从 Redis 读取 JSON 并反序列化为指定类型。
     * 使用 StringRedisTemplate 读取纯 JSON 字符串，再通过 ObjectMapper 转换，
     * 避免 GenericJackson2JsonRedisSerializer 的 LinkedHashMap 问题。
     */
    public <T> T getObject(String key, Class<T> clazz) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Redis 反序列化失败 key={}, type={}", key, clazz.getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将对象序列化为 JSON 存入 Redis（带过期时间）。
     */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            log.error("Redis 序列化失败 key={}", key, e);
        }
    }

    /**
     * 将对象序列化为 JSON 存入 Redis（永不过期）。
     */
    public void setObject(String key, Object value) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            log.error("Redis 序列化失败 key={}", key, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ============ 计数器 ============

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ============ Set 集合 ============

    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ============ ZSet 有序集合 ============

    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    // ============ String（纯字符串） ============

    public void strSet(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void strSet(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String strGet(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ============ 批量删除 ============

    public Long deleteByPattern(String pattern) {
        Collection<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.delete(keys);
        }
        return 0L;
    }

    // ============ 键计数 ============

    public Long countKeys(String pattern) {
        Collection<String> keys = redisTemplate.keys(pattern);
        return keys != null ? (long) keys.size() : 0L;
    }
}
