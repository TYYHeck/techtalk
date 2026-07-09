package com.techtalk.service;

import com.techtalk.common.Result;

/**
 * 点赞服务
 */
public interface LikeService {

    Result<java.util.Map<String, Object>> toggleLike(Long userId, String targetType, Long targetId);
}
