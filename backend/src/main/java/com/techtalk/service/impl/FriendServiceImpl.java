package com.techtalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techtalk.entity.ChatMessage;
import com.techtalk.entity.Friend;
import com.techtalk.entity.User;
import com.techtalk.mapper.ChatMessageMapper;
import com.techtalk.mapper.FriendMapper;
import com.techtalk.mapper.UserMapper;
import com.techtalk.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    private final FriendMapper friendMapper;
    private final UserMapper userMapper;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public void follow(Long userId, Long targetId) {
        if (userId.equals(targetId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 检查目标用户是否存在
        if (userMapper.selectById(targetId) == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已关注
        Friend existing = friendMapper.findByUserAndFriend(userId, targetId);
        if (existing != null && ("FOLLOWING".equals(existing.getStatus()) || "ACCEPTED".equals(existing.getStatus()))) {
            throw new RuntimeException("已关注该用户");
        }

        if (existing != null) {
            // 之前有关注记录（可能是 REJECTED 或 PENDING），更新为 FOLLOWING
            existing.setStatus("FOLLOWING");
            existing.setUpdatedAt(LocalDateTime.now());
            updateById(existing);
        } else {
            Friend f = new Friend();
            f.setUserId(userId);
            f.setFriendId(targetId);
            f.setStatus("FOLLOWING");
            f.setCreatedAt(LocalDateTime.now());
            f.setUpdatedAt(LocalDateTime.now());
            save(f);
        }
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetId) {
        Friend existing = friendMapper.findByUserAndFriend(userId, targetId);
        if (existing == null || !("FOLLOWING".equals(existing.getStatus()) || "ACCEPTED".equals(existing.getStatus()))) {
            throw new RuntimeException("未关注该用户");
        }
        existing.setStatus("REJECTED");
        existing.setUpdatedAt(LocalDateTime.now());
        updateById(existing);
    }

    @Override
    @Transactional
    public void acceptRequest(Long requestId, Long userId) {
        Friend request = getById(requestId);
        if (request == null) throw new RuntimeException("请求不存在");
        if (!request.getFriendId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"PENDING".equals(request.getStatus())) throw new RuntimeException("请求已处理");

        request.setStatus("FOLLOWING");
        request.setUpdatedAt(LocalDateTime.now());
        updateById(request);

        // 创建双向关系
        Friend reverse = friendMapper.findByUserAndFriend(userId, request.getUserId());
        if (reverse == null) {
            Friend f2 = new Friend();
            f2.setUserId(userId);
            f2.setFriendId(request.getUserId());
            f2.setStatus("FOLLOWING");
            f2.setCreatedAt(LocalDateTime.now());
            f2.setUpdatedAt(LocalDateTime.now());
            save(f2);
        } else {
            reverse.setStatus("FOLLOWING");
            reverse.setUpdatedAt(LocalDateTime.now());
            updateById(reverse);
        }
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        Friend request = getById(requestId);
        if (request == null) throw new RuntimeException("请求不存在");
        if (!request.getFriendId().equals(userId)) throw new RuntimeException("无权操作");
        request.setStatus("REJECTED");
        request.setUpdatedAt(LocalDateTime.now());
        updateById(request);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("friend_id", friendId).in("status", "FOLLOWING", "ACCEPTED");
        Friend f1 = getOne(qw);
        if (f1 != null) {
            f1.setStatus("REJECTED");
            updateById(f1);
        }

        QueryWrapper<Friend> qw2 = new QueryWrapper<>();
        qw2.eq("user_id", friendId).eq("friend_id", userId).in("status", "FOLLOWING", "ACCEPTED");
        Friend f2 = getOne(qw2);
        if (f2 != null) {
            f2.setStatus("REJECTED");
            updateById(f2);
        }
    }

    @Override
    public List<Map<String, Object>> getFriends(Long userId) {
        List<Friend> friends = friendMapper.findMutualFriendsByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend f : friends) {
            User friendUser = userMapper.selectById(f.getFriendId());
            if (friendUser == null) continue;
            Map<String, Object> info = buildUserInfo(friendUser);
            info.put("friendId", f.getId());
            info.put("since", f.getCreatedAt());
            // 标注是否为互关
            info.put("isMutual", true);
            result.add(info);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getFollowing(Long userId) {
        List<Friend> following = friendMapper.findFollowingByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend f : following) {
            User targetUser = userMapper.selectById(f.getFriendId());
            if (targetUser == null) continue;
            Map<String, Object> info = buildUserInfo(targetUser);
            info.put("since", f.getCreatedAt());
            // 是否互关
            info.put("isMutual", friendMapper.isMutualFollowing(userId, f.getFriendId()));
            result.add(info);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getFollowers(Long userId) {
        List<Friend> followers = friendMapper.findFollowersByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend f : followers) {
            User followerUser = userMapper.selectById(f.getUserId());
            if (followerUser == null) continue;
            Map<String, Object> info = buildUserInfo(followerUser);
            info.put("since", f.getCreatedAt());
            // 是否互关
            info.put("isMutual", friendMapper.isMutualFollowing(userId, f.getUserId()));
            result.add(info);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<Friend> requests = friendMapper.findPendingRequests(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend f : requests) {
            User fromUser = userMapper.selectById(f.getUserId());
            if (fromUser == null) continue;
            Map<String, Object> info = buildUserInfo(fromUser);
            info.put("requestId", f.getId());
            info.put("createdAt", f.getCreatedAt());
            result.add(info);
        }
        return result;
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        return friendMapper.isMutualFollowing(userId, friendId);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetId) {
        return friendMapper.isFollowing(userId, targetId);
    }

    /**
     * 判断是否可以发私信：
     * 条件1：双方互关（互相关注）
     * 条件2：对方关注了你（你被对方关注）
     * 条件3：你们之间已有过聊天记录（对方回复过你）
     * 满足任一条件即可
     */
    @Override
    public boolean canSendMessage(Long senderId, Long receiverId) {
        // 条件1：互关
        if (friendMapper.isMutualFollowing(senderId, receiverId)) {
            return true;
        }

        // 条件2：对方关注了你
        if (friendMapper.isFollowing(receiverId, senderId)) {
            return true;
        }

        // 条件3：已有过聊天记录（对方回复过）
        String convId = senderId < receiverId ? senderId + "_" + receiverId : receiverId + "_" + senderId;
        ChatMessage lastMsg = chatMessageMapper.findLastMessage(convId);
        if (lastMsg != null) {
            return true;
        }

        return false;
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("avatar", user.getAvatar());
        info.put("bio", user.getBio());
        info.put("postCount", user.getPostCount());
        info.put("likeCount", user.getLikeCount());
        return info;
    }
}
