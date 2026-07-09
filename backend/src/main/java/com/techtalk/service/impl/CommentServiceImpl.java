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

    @Override
    @Transactional
    public Result<CommentVO> createComment(Long userId, CommentDTO dto) {
        // 敏感词过滤
        if (sensitiveWordFilter.containsSensitiveWord(dto.getContent())) {
            return Result.badRequest("评论包含敏感词");
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

        // 更新帖子评论数
        postMapper.incrementCommentCount(dto.getPostId());

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
        Long id = (Long) row.get("id");
        UserVO user = UserVO.builder()
                .id((Long) row.get("user_id"))
                .username((String) row.get("user_username"))
                .avatar((String) row.get("user_avatar"))
                .build();

        UserVO replyToUser = null;
        if (row.get("reply_to_user_id") != null) {
            replyToUser = UserVO.builder()
                    .id((Long) row.get("reply_to_user_id"))
                    .username((String) row.get("reply_username"))
                    .build();
        }

        return CommentVO.builder()
                .id(id)
                .content((String) row.get("content"))
                .postId((Long) row.get("post_id"))
                .parentId((Long) row.get("parent_id"))
                .user(user)
                .replyToUser(replyToUser)
                .likeCount((Integer) row.get("like_count"))
                .isLiked(likedIds.contains(id))
                .createdAt((java.time.LocalDateTime) row.get("created_at"))
                .build();
    }
}
