package com.techtalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.entity.Notification;
import com.techtalk.mapper.NotificationMapper;
import com.techtalk.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Async
    public void createNotification(Long userId, Long fromUserId, String type,
                                    String title, String content, Long postId, Long commentId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setPostId(postId);
        notification.setCommentId(commentId);
        notification.setIsRead(false);

        notificationMapper.insert(notification);
    }

    @Override
    public Result<PageResult<Notification>> getUserNotifications(Long userId, Long page, Long size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        Page<Notification> pageObj = new Page<>(page, size);
        Page<Notification> result = notificationMapper.selectPage(pageObj, wrapper);

        return Result.ok(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @Override
    public Result<Long> getUnreadCount(Long userId) {
        Long count = notificationMapper.countUnread(userId);
        return Result.ok(count);
    }

    @Override
    public Result<Void> markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        return Result.ok();
    }
}
