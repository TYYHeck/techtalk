package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle/{postId}")
    public Result<Map<String, Object>> toggleFavorite(@AuthenticationPrincipal CurrentUser currentUser,
                                                       @PathVariable Long postId) {
        return favoriteService.toggleFavorite(currentUser.getUserId(), postId);
    }

    @GetMapping
    public Result<?> getUserFavorites(@AuthenticationPrincipal CurrentUser currentUser,
                                       @RequestParam(defaultValue = "1") Long page,
                                       @RequestParam(defaultValue = "10") Long size) {
        return favoriteService.getUserFavorites(currentUser.getUserId(), page, size);
    }
}
