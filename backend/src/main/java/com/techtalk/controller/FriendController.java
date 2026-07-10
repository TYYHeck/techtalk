package com.techtalk.controller;

import com.techtalk.security.CurrentUser;
import com.techtalk.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 关注用户 */
    @PostMapping("/follow/{targetId}")
    public ResponseEntity<Map<String, Object>> follow(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long targetId) {
        friendService.follow(user.getUserId(), targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已关注");
        return ResponseEntity.ok(result);
    }

    /** 取消关注 */
    @PostMapping("/unfollow/{targetId}")
    public ResponseEntity<Map<String, Object>> unfollow(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long targetId) {
        friendService.unfollow(user.getUserId(), targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已取消关注");
        return ResponseEntity.ok(result);
    }

    /** 接受好友请求（兼容旧逻辑） */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Map<String, Object>> acceptRequest(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long requestId) {
        friendService.acceptRequest(requestId, user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已接受好友请求");
        return ResponseEntity.ok(result);
    }

    /** 拒绝好友请求 */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Map<String, Object>> rejectRequest(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long requestId) {
        friendService.rejectRequest(requestId, user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已拒绝");
        return ResponseEntity.ok(result);
    }

    /** 移除好友/取消互关 */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Map<String, Object>> removeFriend(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long friendId) {
        friendService.removeFriend(user.getUserId(), friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已移除");
        return ResponseEntity.ok(result);
    }

    /** 获取好友列表（互关用户） */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFriends(@AuthenticationPrincipal CurrentUser user) {
        List<Map<String, Object>> friends = friendService.getFriends(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", friends);
        return ResponseEntity.ok(result);
    }

    /** 获取关注列表 */
    @GetMapping("/following")
    public ResponseEntity<Map<String, Object>> getFollowing(@AuthenticationPrincipal CurrentUser user) {
        List<Map<String, Object>> following = friendService.getFollowing(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", following);
        return ResponseEntity.ok(result);
    }

    /** 获取粉丝列表 */
    @GetMapping("/followers")
    public ResponseEntity<Map<String, Object>> getFollowers(@AuthenticationPrincipal CurrentUser user) {
        List<Map<String, Object>> followers = friendService.getFollowers(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", followers);
        return ResponseEntity.ok(result);
    }

    /** 获取待处理的好友请求 */
    @GetMapping("/requests")
    public ResponseEntity<Map<String, Object>> getPendingRequests(@AuthenticationPrincipal CurrentUser user) {
        List<Map<String, Object>> requests = friendService.getPendingRequests(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", requests);
        return ResponseEntity.ok(result);
    }

    /** 检查是否为互关好友 */
    @GetMapping("/check/{friendId}")
    public ResponseEntity<Map<String, Object>> checkFriend(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long friendId) {
        boolean isFriend = friendService.isFriend(user.getUserId(), friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", isFriend);
        return ResponseEntity.ok(result);
    }

    /** 检查是否可以发私信 */
    @GetMapping("/can-message/{targetId}")
    public ResponseEntity<Map<String, Object>> canSendMessage(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long targetId) {
        boolean can = friendService.canSendMessage(user.getUserId(), targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", can);
        return ResponseEntity.ok(result);
    }
}
