package com.techtalk.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtalk.entity.ChatMessage;
import com.techtalk.service.ChatMessageService;
import com.techtalk.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final FriendService friendService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** userId -> WebSocketSession */
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket connected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) return;

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = payload.get("content").toString();

        // 检查聊天权限
        if (!friendService.canSendMessage(senderId, receiverId)) {
            Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("type", "error");
            error.put("message", "需要互相关注、对方关注你或有聊天记录才能发送私信");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
            return;
        }

        // 保存消息
        ChatMessage msg = chatMessageService.sendMessage(senderId, receiverId, content);

        // 发送给接收者
        WebSocketSession receiverSession = sessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            Map<String, Object> msgData = new java.util.LinkedHashMap<>();
            msgData.put("type", "message");
            msgData.put("id", msg.getId());
            msgData.put("senderId", senderId);
            msgData.put("receiverId", receiverId);
            msgData.put("content", content);
            msgData.put("conversationId", msg.getConversationId());
            msgData.put("isRead", msg.getIsRead());
            msgData.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
            receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(msgData)));
        }

        // 回执给发送者
        Map<String, Object> ack = new java.util.LinkedHashMap<>();
        ack.put("type", "ack");
        ack.put("id", msg.getId());
        ack.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    public static boolean isUserOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }
}
