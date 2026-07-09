package com.techtalk.service;

import com.techtalk.common.Result;

/**
 * 收藏服务
 */
public interface FavoriteService {

    Result<java.util.Map<String, Object>> toggleFavorite(Long userId, Long postId);

    Result<com.techtalk.common.PageResult<com.techtalk.vo.PostVO>> getUserFavorites(Long userId, Long page, Long size);
}
