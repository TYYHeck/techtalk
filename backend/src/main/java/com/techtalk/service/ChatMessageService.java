package com.techtalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techtalk.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatMessageService extends IService<ChatMessage> {

    ChatMessage sendMessage(Long senderId, Long receiverId, String content);

    List<ChatMessage> getMessages(Long userId, Long otherUserId);

    List<Map<String, Object>> getConversations(Long userId);

    void markAsRead(String conversationId, Long userId);

    int getUnreadCount(Long userId);

    String buildConversationId(Long user1, Long user2);
}
