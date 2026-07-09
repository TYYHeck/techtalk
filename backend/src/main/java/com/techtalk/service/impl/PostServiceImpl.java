package com.techtalk.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.BusinessException;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.dto.PostDTO;
import com.techtalk.entity.Post;
import com.techtalk.entity.User;
import com.techtalk.mapper.CategoryMapper;
import com.techtalk.mapper.LikeRecordMapper;
import com.techtalk.mapper.PostMapper;
import com.techtalk.mapper.UserMapper;
import com.techtalk.service.PostService;
import com.techtalk.util.RedisUtil;
import com.techtalk.util.SensitiveWordFilter;
import com.techtalk.vo.PostVO;
import com.techtalk.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final RedisUtil redisUtil;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional
    public Result<PostVO> createPost(Long userId, PostDTO dto) {
        // 敏感词检查
        if (sensitiveWordFilter.containsSensitiveWord(dto.getTitle()) ||
            sensitiveWordFilter.containsSensitiveWord(dto.getContent())) {
            return Result.badRequest("内容包含敏感词");
        }

        // 检查分类
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            return Result.badRequest("分类不存在");
        }

        Post post = new Post();
        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent());
        post.setSummary(dto.getSummary() != null ? dto.getSummary()
                : extractSummary(dto.getContent()));
        post.setCategoryId(dto.getCategoryId());
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setIsPinned(false);
        post.setIsFeatured(false);
        post.setStatus("PUBLISHED");

        if (postMapper.insert(post) <= 0) {
            throw new BusinessException("发布失败");
        }

        // 更新用户发帖数 + 分类帖子数
        userMapper.incrementPostCount(userId);
        categoryMapper.incrementPostCount(dto.getCategoryId());

        log.info("用户 {} 发布了帖子: {}", userId, post.getTitle());
        return Result.ok("发布成功", buildPostVO(post, userId));
    }

    @Override
    @Transactional
    public Result<PostVO> updatePost(Long userId, Long postId, PostDTO dto) {
        Post post = postMapper.selectByIdWithDeleted(postId);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return Result.forbidden("无权编辑此帖子");
        }

        if (sensitiveWordFilter.containsSensitiveWord(dto.getTitle()) ||
            sensitiveWordFilter.containsSensitiveWord(dto.getContent())) {
            return Result.badRequest("内容包含敏感词");
        }

        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent());
        post.setSummary(dto.getSummary() != null ? dto.getSummary()
                : extractSummary(dto.getContent()));
        post.setCategoryId(dto.getCategoryId());

        postMapper.updateById(post);
        return Result.ok("编辑成功", buildPostVO(post, userId));
    }

    @Override
    @Transactional
    public Result<Void> deletePost(Long userId, Long postId) {
        Post post = postMapper.selectByIdWithDeleted(postId);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        // 作者可以删除，管理员也可以（在 Controller 层判断角色）
        if (!post.getUserId().equals(userId)) {
            return Result.forbidden("无权删除此帖子");
        }

        postMapper.deleteById(postId);

        // 更新计数
        userMapper.decrementPostCount(post.getUserId());
        categoryMapper.decrementPostCount(post.getCategoryId());

        // 清除缓存
        redisUtil.delete("post:detail:" + postId);
        redisUtil.deleteByPattern("post:list:*");

        log.info("用户 {} 删除了帖子: {}", userId, postId);
        return Result.ok();
    }

    @Override
    public Result<PostVO> getPostById(Long postId, Long currentUserId) {
        // 先查缓存
        String cacheKey = "post:detail:" + postId;
        PostVO cached = redisUtil.get(cacheKey);
        if (cached != null) {
            // 异步更新浏览量
            postMapper.incrementViewCount(postId);
            return Result.ok(cached);
        }

        Post post = postMapper.selectByIdWithDeleted(postId);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }

        // 浏览量 +1
        postMapper.incrementViewCount(postId);

        PostVO vo = buildPostVO(post, currentUserId);

        // 缓存1小时
        redisUtil.set(cacheKey, vo, 1, TimeUnit.HOURS);

        return Result.ok(vo);
    }

    @Override
    public Result<PageResult<PostVO>> getPostList(Long page, Long size, Long categoryId,
                                                    String keyword, Long currentUserId) {
        // 尝试从缓存获取首页
        if (page == 1 && size == 10 && categoryId == null && keyword == null) {
            String cacheKey = "post:list:page1";
            PageResult<PostVO> cached = redisUtil.get(cacheKey);
            if (cached != null) {
                return Result.ok(cached);
            }
        }

        Page<Post> pageObj = new Page<>(page, size);
        Page<Map<String, Object>> resultPage = postMapper.selectPublishedPage(
                pageObj, categoryId, keyword);

        Set<Long> likedPostIds = currentUserId != null
                ? likeRecordMapper.selectLikedPostIdsByUserId(currentUserId)
                : Collections.emptySet();

        List<PostVO> voList = resultPage.getRecords().stream().map(row -> {
            PostVO vo = new PostVO();
            vo.setId((Long) row.get("id"));
            vo.setTitle((String) row.get("title"));
            vo.setSummary((String) row.get("summary"));
            vo.setContent(null); // 列表不返回内容
            vo.setCategoryId((Long) row.get("category_id"));
            vo.setCategoryName((String) row.get("category_name"));
            vo.setViewCount((Integer) row.get("view_count"));
            vo.setLikeCount((Integer) row.get("like_count"));
            vo.setCommentCount((Integer) row.get("comment_count"));
            vo.setFavoriteCount((Integer) row.get("favorite_count"));
            vo.setIsPinned((Boolean) row.get("is_pinned"));
            vo.setIsFeatured((Boolean) row.get("is_featured"));
            vo.setCreatedAt((java.time.LocalDateTime) row.get("created_at"));
            vo.setUpdatedAt((java.time.LocalDateTime) row.get("updated_at"));

            // 作者信息
            UserVO author = UserVO.builder()
                    .id((Long) row.get("user_id"))
                    .username((String) row.get("author_username"))
                    .avatar((String) row.get("author_avatar"))
                    .build();
            vo.setAuthor(author);

            vo.setIsLiked(likedPostIds.contains(vo.getId()));
            return vo;
        }).collect(Collectors.toList());

        PageResult<PostVO> pageResult = PageResult.of(
                voList, resultPage.getTotal(), page, size);

        // 缓存首页 5 分钟
        if (page == 1 && size == 10 && categoryId == null && keyword == null) {
            redisUtil.set("post:list:page1", pageResult, 5, TimeUnit.MINUTES);
        }

        return Result.ok(pageResult);
    }

    // ========== 私有方法 ==========

    private PostVO buildPostVO(Post post, Long currentUserId) {
        User user = userMapper.selectById(post.getUserId());

        UserVO author = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .build();

        boolean isLiked = currentUserId != null &&
                likeRecordMapper.countByUserAndTarget(currentUserId, "POST", post.getId()) > 0;

        return PostVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .categoryId(post.getCategoryId())
                .author(author)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .favoriteCount(post.getFavoriteCount())
                .isPinned(post.getIsPinned())
                .isFeatured(post.getIsFeatured())
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private String extractSummary(String content) {
        // 去除 HTML 标签，取前 200 字符
        String plainText = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
    }
}
