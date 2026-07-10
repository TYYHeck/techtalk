package com.techtalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techtalk.entity.ChatMessage;
import com.techtalk.entity.User;
import com.techtalk.mapper.ChatMessageMapper;
import com.techtalk.mapper.UserMapper;
import com.techtalk.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(buildConversationId(senderId, receiverId));
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(false);
        msg.setCreatedAt(LocalDateTime.now());
        save(msg);
        return msg;
    }

    @Override
    public List<ChatMessage> getMessages(Long userId, Long otherUserId) {
        String convId = buildConversationId(userId, otherUserId);
        markAsRead(convId, userId);
        return chatMessageMapper.findByConversationId(convId);
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        List<String> convIds = chatMessageMapper.findConversationsByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String convId : convIds) {
            ChatMessage lastMsg = chatMessageMapper.findLastMessage(convId);
            if (lastMsg == null) continue;

            Long otherUserId = lastMsg.getSenderId().equals(userId) ? lastMsg.getReceiverId() : lastMsg.getSenderId();
            User otherUser = userMapper.selectById(otherUserId);
            if (otherUser == null) continue;

            Map<String, Object> conv = new LinkedHashMap<>();
            conv.put("conversationId", convId);
            conv.put("otherUser", buildUserInfo(otherUser));
            conv.put("lastMessage", lastMsg);
            conv.put("unreadCount", countUnreadForConv(convId, userId));
            result.add(conv);
        }
        return result;
    }

    private int countUnreadForConv(String convId, Long userId) {
        // 简单统计该会话中 receiver=userId 且未读的消息数
        List<ChatMessage> msgs = chatMessageMapper.findByConversationId(convId);
        return (int) msgs.stream().filter(m -> m.getReceiverId().equals(userId) && !m.getIsRead()).count();
    }

    @Override
    public void markAsRead(String conversationId, Long userId) {
        chatMessageMapper.markAsRead(conversationId, userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return chatMessageMapper.countUnread(userId);
    }

    @Override
    public String buildConversationId(Long user1, Long user2) {
        return user1 < user2 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("avatar", user.getAvatar());
        info.put("bio", user.getBio());
        return info;
    }
}
