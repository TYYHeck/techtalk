package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleLike(@AuthenticationPrincipal CurrentUser currentUser,
                                                   @RequestParam String targetType,
                                                   @RequestParam Long targetId) {
        return likeService.toggleLike(currentUser.getUserId(), targetType, targetId);
    }
}
