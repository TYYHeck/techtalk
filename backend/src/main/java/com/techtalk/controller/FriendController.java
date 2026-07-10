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

    /** 发送好友请求 */
    @PostMapping("/request/{friendId}")
    public ResponseEntity<Map<String, Object>> sendRequest(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long friendId) {
        friendService.sendRequest(user.getUserId(), friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "好友请求已发送");
        return ResponseEntity.ok(result);
    }

    /** 接受好友请求 */
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

    /** 删除好友 */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Map<String, Object>> removeFriend(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long friendId) {
        friendService.removeFriend(user.getUserId(), friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已删除好友");
        return ResponseEntity.ok(result);
    }

    /** 获取好友列表 */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFriends(@AuthenticationPrincipal CurrentUser user) {
        List<Map<String, Object>> friends = friendService.getFriends(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", friends);
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

    /** 检查是否为好友 */
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
}
