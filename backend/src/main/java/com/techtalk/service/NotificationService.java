package com.techtalk.service;

import com.techtalk.common.Result;

/**
 * 通知服务
 */
public interface NotificationService {

    void createNotification(Long userId, Long fromUserId, String type,
                             String title, String content, Long postId, Long commentId);

    Result<?> getUserNotifications(Long userId, Long page, Long size);

    Result<Long> getUnreadCount(Long userId);

    Result<Void> markAllAsRead(Long userId);
}
