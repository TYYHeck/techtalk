package com.techtalk.service;

import com.techtalk.common.Result;
import com.techtalk.dto.PostDTO;
import com.techtalk.vo.PostVO;

/**
 * 帖子服务
 */
public interface PostService {

    Result<PostVO> createPost(Long userId, PostDTO dto);

    Result<PostVO> updatePost(Long userId, Long postId, PostDTO dto);

    Result<Void> deletePost(Long userId, Long postId);

    Result<PostVO> getPostById(Long postId, Long currentUserId);

    Result<?> getPostList(Long page, Long size, Long categoryId,
                           String keyword, Long currentUserId);
}
