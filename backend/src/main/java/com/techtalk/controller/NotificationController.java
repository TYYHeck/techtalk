package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<?> getNotifications(@AuthenticationPrincipal CurrentUser currentUser,
                                       @RequestParam(defaultValue = "1") Long page,
                                       @RequestParam(defaultValue = "10") Long size) {
        return notificationService.getUserNotifications(currentUser.getUserId(), page, size);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@AuthenticationPrincipal CurrentUser currentUser) {
        return notificationService.getUnreadCount(currentUser.getUserId());
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal CurrentUser currentUser) {
        return notificationService.markAllAsRead(currentUser.getUserId());
    }
}
