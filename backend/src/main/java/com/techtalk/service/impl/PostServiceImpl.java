package com.techtalk.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.BusinessException;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.dto.PostDTO;
import com.techtalk.entity.Post;
import com.techtalk.entity.PostTag;
import com.techtalk.entity.User;
import com.techtalk.mapper.CategoryMapper;
import com.techtalk.mapper.LikeRecordMapper;
import com.techtalk.mapper.PostMapper;
import com.techtalk.mapper.PostTagMapper;
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
    private final PostTagMapper postTagMapper;
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

        // 取 categoryIds 列表，若为空则用 categoryId 兜底
        List<Long> categoryIds = dto.getCategoryIds();
        if (categoryIds == null || categoryIds.isEmpty()) {
            if (dto.getCategoryId() != null) {
                categoryIds = List.of(dto.getCategoryId());
            } else {
                return Result.badRequest("请至少选择一个分类");
            }
        }

        // 检查所有分类是否存在
        for (Long cid : categoryIds) {
            if (categoryMapper.selectById(cid) == null) {
                return Result.badRequest("分类不存在");
            }
        }

        Long primaryCategoryId = dto.getCategoryId() != null ? dto.getCategoryId() : categoryIds.get(0);

        Post post = new Post();
        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent());
        post.setSummary(dto.getSummary() != null ? dto.getSummary()
                : extractSummary(dto.getContent()));
        post.setCategoryId(primaryCategoryId);
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

        // 插入多分类关联
        for (Long cid : categoryIds) {
            PostTag pt = new PostTag();
            pt.setPostId(post.getId());
            pt.setCategoryId(cid);
            postTagMapper.insert(pt);
        }

        // 更新用户发帖数 + 各分类帖子数
        userMapper.incrementPostCount(userId);
        for (Long cid : categoryIds) {
            categoryMapper.incrementPostCount(cid);
        }

        log.info("用户 {} 发布了帖子: {}, 分类: {}", userId, post.getTitle(), categoryIds);
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

        // 取 categoryIds 列表
        List<Long> categoryIds = dto.getCategoryIds();
        if (categoryIds == null || categoryIds.isEmpty()) {
            if (dto.getCategoryId() != null) {
                categoryIds = List.of(dto.getCategoryId());
            } else {
                return Result.badRequest("请至少选择一个分类");
            }
        }

        for (Long cid : categoryIds) {
            if (categoryMapper.selectById(cid) == null) {
                return Result.badRequest("分类不存在");
            }
        }

        Long primaryCategoryId = dto.getCategoryId() != null ? dto.getCategoryId() : categoryIds.get(0);

        // 更新分类关联
        postTagMapper.deleteByPostId(postId);
        for (Long cid : categoryIds) {
            PostTag pt = new PostTag();
            pt.setPostId(postId);
            pt.setCategoryId(cid);
            postTagMapper.insert(pt);
        }

        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent());
        post.setSummary(dto.getSummary() != null ? dto.getSummary()
                : extractSummary(dto.getContent()));
        post.setCategoryId(primaryCategoryId);

        postMapper.updateById(post);

        // 清除缓存
        redisUtil.delete("post:detail:" + postId);
        redisUtil.deleteByPattern("post:list:*");

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
        PostVO cached = redisUtil.getObject(cacheKey, PostVO.class);
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
        redisUtil.setObject(cacheKey, vo, 1, TimeUnit.HOURS);

        return Result.ok(vo);
    }

    @Override
    public Result<PageResult<PostVO>> getPostList(Long page, Long size, Long categoryId,
                                                    String keyword, Long currentUserId) {
        // 尝试从缓存获取首页
        if (page == 1 && size == 10 && categoryId == null && keyword == null) {
            String cacheKey = "post:list:page1";
            PageResult<PostVO> cached = redisUtil.getObject(cacheKey, PageResult.class);
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
            vo.setId(getLong(row, "id"));
            vo.setTitle(getStr(row, "title"));
            vo.setSummary(getStr(row, "summary"));
            vo.setContent(null); // 列表不返回内容
            vo.setCategoryId(getLong(row, "categoryId"));
            vo.setCategoryName(getStr(row, "categoryName"));
            vo.setViewCount(getInt(row, "viewCount"));
            vo.setLikeCount(getInt(row, "likeCount"));
            vo.setCommentCount(getInt(row, "commentCount"));
            vo.setFavoriteCount(getInt(row, "favoriteCount"));
            Object isPinnedObj = row.get("isPinned");
            vo.setIsPinned(isPinnedObj instanceof Boolean ? (Boolean) isPinnedObj : (isPinnedObj instanceof Number && ((Number) isPinnedObj).intValue() != 0));
            Object isFeaturedObj = row.get("isFeatured");
            vo.setIsFeatured(isFeaturedObj instanceof Boolean ? (Boolean) isFeaturedObj : (isFeaturedObj instanceof Number && ((Number) isFeaturedObj).intValue() != 0));
            Object createdAtObj = row.get("createdAt");
            if (createdAtObj == null) createdAtObj = row.get("created_at");
            vo.setCreatedAt(toLocalDateTime(createdAtObj));
            Object updatedAtObj = row.get("updatedAt");
            if (updatedAtObj == null) updatedAtObj = row.get("updated_at");
            vo.setUpdatedAt(toLocalDateTime(updatedAtObj));

            // 作者信息
            UserVO author = UserVO.builder()
                    .id(getLong(row, "userId"))
                    .username(getStr(row, "authorUsername"))
                    .avatar(getStr(row, "authorAvatar"))
                    .build();
            vo.setAuthor(author);

            vo.setIsLiked(likedPostIds.contains(vo.getId()));
            return vo;
        }).collect(Collectors.toList());

        // 批量加载分类标签
        if (!voList.isEmpty()) {
            List<Long> postIds = voList.stream().map(PostVO::getId).collect(Collectors.toList());
            List<Map<String, Object>> tagRows = postTagMapper.selectCategoriesByPostIds(postIds);
            Map<Long, List<PostVO.CategoryTagVO>> tagMap = new HashMap<>();
            for (Map<String, Object> row : tagRows) {
                Long pid = getLong(row, "post_id");
                PostVO.CategoryTagVO tag = PostVO.CategoryTagVO.builder()
                        .id(getLong(row, "category_id"))
                        .name(getStr(row, "category_name"))
                        .icon(getStr(row, "category_icon"))
                        .build();
                tagMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(tag);
            }
            for (PostVO vo : voList) {
                vo.setCategories(tagMap.getOrDefault(vo.getId(), Collections.emptyList()));
            }
        }

        PageResult<PostVO> pageResult = PageResult.of(
                voList, resultPage.getTotal(), page, size);

        // 缓存首页 5 分钟
        if (page == 1 && size == 10 && categoryId == null && keyword == null) {
            redisUtil.setObject("post:list:page1", pageResult, 5, TimeUnit.MINUTES);
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

        // 获取多分类标签
        List<PostVO.CategoryTagVO> categoryTags = getCategoryTags(post.getId());

        return PostVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .categoryId(post.getCategoryId())
                .categoryName(categoryTags.isEmpty() ? null : categoryTags.get(0).getName())
                .categories(categoryTags)
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

    /** 获取帖子的分类标签列表 */
    private List<PostVO.CategoryTagVO> getCategoryTags(Long postId) {
        List<Map<String, Object>> rows = postTagMapper.selectCategoriesByPostId(postId);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        return rows.stream().map(row -> PostVO.CategoryTagVO.builder()
                .id(getLong(row, "category_id"))
                .name(getStr(row, "category_name"))
                .icon(getStr(row, "category_icon"))
                .build()).collect(Collectors.toList());
    }

    private String extractSummary(String content) {
        // 去除 HTML 标签，取前 200 字符
        String plainText = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
    }

    /** 从 Map 安全读取 String，兼容驼峰和下划线命名 */
    private String getStr(Map<String, Object> map, String camelKey) {
        Object val = map.get(camelKey);
        if (val == null) {
            // 尝试下划线命名
            String underscoreKey = camelToUnderscore(camelKey);
            val = map.get(underscoreKey);
        }
        return val != null ? val.toString() : null;
    }

    /** 从 Map 安全读取 Long，兼容驼峰和下划线命名 */
    private Long getLong(Map<String, Object> map, String camelKey) {
        Object val = map.get(camelKey);
        if (val == null) {
            val = map.get(camelToUnderscore(camelKey));
        }
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    /** 从 Map 安全读取 Integer，兼容驼峰和下划线命名 */
    private Integer getInt(Map<String, Object> map, String camelKey) {
        Object val = map.get(camelKey);
        if (val == null) {
            val = map.get(camelToUnderscore(camelKey));
        }
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        return null;
    }

    /** 驼峰转下划线 */
    private String camelToUnderscore(String camel) {
        return camel.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

    /** 将 Timestamp/LocalDateTime/String 统一转为 LocalDateTime */
    private java.time.LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) obj;
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        if (obj instanceof java.util.Date) return ((java.util.Date) obj).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        if (obj instanceof String) {
            String s = ((String) obj).trim();
            if (s.isEmpty()) return null;
            try {
                return java.time.LocalDateTime.parse(s.replace(" ", "T"));
            } catch (Exception e) {
                log.warn("无法解析日期字符串: {}", s);
                return null;
            }
        }
        return null;
    }
}
