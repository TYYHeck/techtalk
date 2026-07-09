package com.techtalk.service.impl;

import com.techtalk.common.Result;
import com.techtalk.entity.LikeRecord;
import com.techtalk.mapper.CommentMapper;
import com.techtalk.mapper.LikeRecordMapper;
import com.techtalk.mapper.PostMapper;
import com.techtalk.mapper.UserMapper;
import com.techtalk.service.LikeService;
import com.techtalk.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 点赞服务实现
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Result<Map<String, Object>> toggleLike(Long userId, String targetType, Long targetId) {
        LikeRecord existing = likeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);

        if (existing != null) {
            // 取消点赞
            likeRecordMapper.deleteById(existing.getId());
            if ("POST".equals(targetType)) {
                postMapper.decrementLikeCount(targetId);
            } else if ("COMMENT".equals(targetType)) {
                commentMapper.decrementLikeCount(targetId);
            }
            return Result.ok(Map.of("liked", false, "message", "取消点赞"));
        } else {
            // 点赞
            LikeRecord record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            likeRecordMapper.insert(record);

            if ("POST".equals(targetType)) {
                postMapper.incrementLikeCount(targetId);
                var post = postMapper.selectByIdWithDeleted(targetId);
                if (post != null && !post.getUserId().equals(userId)) {
                    var user = userMapper.selectById(userId);
                    notificationService.createNotification(
                            post.getUserId(), userId, "LIKE",
                            "新点赞", user.getUsername() + " 点赞了你的帖子",
                            targetId, null);
                }
            } else if ("COMMENT".equals(targetType)) {
                commentMapper.incrementLikeCount(targetId);
            }
            return Result.ok(Map.of("liked", true, "message", "点赞成功"));
        }
    }
}
