package com.techtalk.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.entity.Favorite;
import com.techtalk.mapper.FavoriteMapper;
import com.techtalk.mapper.PostMapper;
import com.techtalk.service.FavoriteService;
import com.techtalk.vo.PostVO;
import com.techtalk.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public Result<Map<String, Object>> toggleFavorite(Long userId, Long postId) {
        Favorite existing = favoriteMapper.selectByUserAndPost(userId, postId);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            postMapper.decrementFavoriteCount(postId);
            return Result.ok(Map.of("favorited", false, "message", "取消收藏"));
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setPostId(postId);
            favoriteMapper.insert(favorite);
            postMapper.incrementFavoriteCount(postId);
            return Result.ok(Map.of("favorited", true, "message", "收藏成功"));
        }
    }

    @Override
    public Result<PageResult<PostVO>> getUserFavorites(Long userId, Long page, Long size) {
        Page<Favorite> pageObj = new Page<>(page, size);
        Page<Map<String, Object>> result = favoriteMapper.selectUserFavorites(pageObj, userId);

        List<PostVO> voList = result.getRecords().stream().map(row -> {
            PostVO vo = new PostVO();
            vo.setId((Long) row.get("id"));
            vo.setTitle((String) row.get("title"));
            vo.setSummary((String) row.get("summary"));
            vo.setLikeCount((Integer) row.get("like_count"));
            vo.setCommentCount((Integer) row.get("comment_count"));
            vo.setCreatedAt((java.time.LocalDateTime) row.get("created_at"));

            vo.setAuthor(UserVO.builder()
                    .id((Long) row.get("user_id"))
                    .username((String) row.get("author_username"))
                    .avatar((String) row.get("author_avatar"))
                    .build());
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(PageResult.of(voList, result.getTotal(), page, size));
    }
}
