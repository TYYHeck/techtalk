package com.techtalk.service.impl;

import com.techtalk.common.BusinessException;
import com.techtalk.common.Result;
import com.techtalk.dto.CommentDTO;
import com.techtalk.entity.Comment;
import com.techtalk.entity.User;
import com.techtalk.mapper.CommentMapper;
import com.techtalk.mapper.LikeRecordMapper;
import com.techtalk.mapper.PostMapper;
import com.techtalk.mapper.UserMapper;
import com.techtalk.service.CommentService;
import com.techtalk.service.NotificationService;
import com.techtalk.util.RedisUtil;
import com.techtalk.util.SensitiveWordFilter;
import com.techtalk.vo.CommentVO;
import com.techtalk.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final NotificationService notificationService;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final RedisUtil redisUtil;

    /** 评论防重：同一用户对同一帖子的相同内容，30秒内不可重复发送 */
    private static final int COMMENT_DEDUP_TTL_SECONDS = 30;

    @Override
    @Transactional
    public Result<CommentVO> createComment(Long userId, CommentDTO dto) {
        // 敏感词过滤
        if (sensitiveWordFilter.containsSensitiveWord(dto.getContent())) {
            return Result.badRequest("评论包含敏感词");
        }

        // 评论防重：同一用户 + 同一帖子 + 相同内容，30秒内不可重复
        String dedupKey = "comment:dedup:" + userId + ":" + dto.getPostId() + ":" + hashContent(dto.getContent());
        if (redisUtil.hasKey(dedupKey)) {
            return Result.fail(429, "评论发送过于频繁，请30秒后再试");
        }

        // 检查帖子存在
        var post = postMapper.selectByIdWithDeleted(dto.getPostId());
        if (post == null) {
            return Result.notFound("帖子不存在");
        }

        Comment comment = new Comment();
        comment.setContent(sensitiveWordFilter.replace(dto.getContent()));
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setLikeCount(0);
        comment.setStatus("PUBLISHED");

        if (commentMapper.insert(comment) <= 0) {
            throw new BusinessException("评论失败");
        }

        // 设置评论防重标记（30秒）
        redisUtil.strSet(dedupKey, "1", COMMENT_DEDUP_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

        // 更新帖子评论数
        postMapper.incrementCommentCount(dto.getPostId());

        // 清除帖子缓存，使前端重新获取最新 commentCount
        redisUtil.delete("post:detail:" + dto.getPostId());
        redisUtil.deleteByPattern("post:list:*");

        // 发送通知（不给自己发）
        if (!post.getUserId().equals(userId)) {
            User commentUser = userMapper.selectById(userId);
            notificationService.createNotification(
                    post.getUserId(), userId, "COMMENT",
                    "新评论", commentUser.getUsername() + " 评论了你的帖子",
                    dto.getPostId(), comment.getId());
        }

        // 如果是回复，通知被回复的人
        if (dto.getReplyToUserId() != null && !dto.getReplyToUserId().equals(userId)) {
            User commentUser = userMapper.selectById(userId);
            notificationService.createNotification(
                    dto.getReplyToUserId(), userId, "REPLY",
                    "新回复", commentUser.getUsername() + " 回复了你的评论",
                    dto.getPostId(), comment.getId());
        }

        return Result.ok(buildCommentVO(comment, userId));
    }

    private CommentVO buildCommentVO(Comment comment, Long currentUserId) {
        User user = userMapper.selectById(comment.getUserId());
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build();

        UserVO replyToUser = null;
        if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
            User replyUser = userMapper.selectById(comment.getReplyToUserId());
            if (replyUser != null) {
                replyToUser = UserVO.builder()
                        .id(replyUser.getId())
                        .username(replyUser.getUsername())
                        .build();
            }
        }

        boolean isLiked = currentUserId != null &&
                likeRecordMapper.countByUserAndTarget(currentUserId, "COMMENT", comment.getId()) > 0;

        return CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .user(userVO)
                .replyToUser(replyToUser)
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .createdAt(comment.getCreatedAt())
                .children(List.of())
                .build();
    }

    @Override
    @Transactional
    public Result<Void> deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.notFound("评论不存在");
        }

        // 作者或帖子作者可删除
        var post = postMapper.selectByIdWithDeleted(comment.getPostId());
        if (!comment.getUserId().equals(userId) &&
            (post == null || !post.getUserId().equals(userId))) {
            return Result.forbidden("无权删除此评论");
        }

        commentMapper.deleteById(commentId);
        postMapper.decrementCommentCount(comment.getPostId());

        // 清除帖子缓存
        redisUtil.delete("post:detail:" + comment.getPostId());
        redisUtil.deleteByPattern("post:list:*");

        return Result.ok();
    }

    @Override
    public Result<List<CommentVO>> getCommentsByPostId(Long postId, Long currentUserId) {
        // 获取顶级评论
        List<Map<String, Object>> topLevel = commentMapper.selectTopLevelByPostId(postId);

        Set<Long> likedCommentIds = currentUserId != null
                ? likeRecordMapper.selectLikedCommentIdsByUserId(currentUserId)
                : Collections.emptySet();

        List<CommentVO> voList = topLevel.stream().map(row -> {
            CommentVO vo = mapRowToVO(row, likedCommentIds);

            // 获取子评论
            List<Map<String, Object>> children = commentMapper.selectByParentId(vo.getId());
            List<CommentVO> childVOs = children.stream()
                    .map(r -> mapRowToVO(r, likedCommentIds))
                    .collect(Collectors.toList());
            vo.setChildren(childVOs);

            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList);
    }

    private CommentVO mapRowToVO(Map<String, Object> row, Set<Long> likedIds) {
        Long id = getLong(row, "id");
        UserVO user = UserVO.builder()
                .id(getLong(row, "userId"))
                .username(getStr(row, "userUsername"))
                .avatar(getStr(row, "userAvatar"))
                .build();

        UserVO replyToUser = null;
        Long replyToUserId = getLong(row, "replyToUserId");
        if (replyToUserId != null) {
            replyToUser = UserVO.builder()
                    .id(replyToUserId)
                    .username(getStr(row, "replyUsername"))
                    .build();
        }

        Object createdAtObj = row.get("createdAt");
        if (createdAtObj == null) createdAtObj = row.get("created_at");

        return CommentVO.builder()
                .id(id)
                .content(getStr(row, "content"))
                .postId(getLong(row, "postId"))
                .parentId(getLong(row, "parentId"))
                .user(user)
                .replyToUser(replyToUser)
                .likeCount(getInt(row, "likeCount"))
                .isLiked(likedIds.contains(id))
                .createdAt(toLocalDateTime(createdAtObj))
                .build();
    }

    /** 从 Map 安全读取 String，兼容驼峰和下划线命名 */
    private String getStr(Map<String, Object> map, String camelKey) {
        Object val = map.get(camelKey);
        if (val == null) {
            val = map.get(camelToUnderscore(camelKey));
        }
        return val != null ? val.toString() : null;
    }

    /** 从 Map 安全读取 Long */
    private Long getLong(Map<String, Object> map, String camelKey) {
        Object val = map.get(camelKey);
        if (val == null) {
            val = map.get(camelToUnderscore(camelKey));
        }
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    /** 从 Map 安全读取 Integer */
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

    /** 将 Timestamp/LocalDateTime 统一转为 LocalDateTime */
    private java.time.LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) obj;
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        if (obj instanceof java.util.Date) return ((java.util.Date) obj).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return null;
    }

    /**
     * 对评论内容做简化 hash，用于防重判断（去空格、转小写、取 MD5 前16位）
     */
    private String hashContent(String content) {
        if (content == null) return "0";
        String normalized = content.trim().toLowerCase().replaceAll("\\s+", " ");
        int hash = normalized.hashCode();
        return Integer.toHexString(hash);
    }
}
