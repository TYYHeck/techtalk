package com.techtalk.controller;

import com.techtalk.entity.ChatMessage;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.ChatMessageService;
import com.techtalk.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final FriendService friendService;

    /** 获取会话列表 */
    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getConversations(@AuthenticationPrincipal CurrentUser user) {
        List<Map<String, Object>> conversations = chatMessageService.getConversations(user.getUserId());
        int unreadCount = chatMessageService.getUnreadCount(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", conversations);
        result.put("unreadCount", unreadCount);
        return ResponseEntity.ok(result);
    }

    /** 获取与某用户的聊天记录 */
    @GetMapping("/messages/{otherUserId}")
    public ResponseEntity<Map<String, Object>> getMessages(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long otherUserId) {
        List<ChatMessage> messages = chatMessageService.getMessages(user.getUserId(), otherUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", messages);
        return ResponseEntity.ok(result);
    }

    /** 获取未读消息数 */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal CurrentUser user) {
        int count = chatMessageService.getUnreadCount(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", count);
        return ResponseEntity.ok(result);
    }

    /** 标记会话已读 */
    @PostMapping("/read/{otherUserId}")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long otherUserId) {
        String convId = chatMessageService.buildConversationId(user.getUserId(), otherUserId);
        chatMessageService.markAsRead(convId, user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "ok");
        return ResponseEntity.ok(result);
    }

    /** 检查是否可以向某用户发私信 */
    @GetMapping("/check/{targetId}")
    public ResponseEntity<Map<String, Object>> checkCanMessage(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long targetId) {
        boolean can = friendService.canSendMessage(user.getUserId(), targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", can);
        return ResponseEntity.ok(result);
    }
}
