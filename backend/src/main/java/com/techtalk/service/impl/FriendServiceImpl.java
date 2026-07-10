package com.techtalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techtalk.entity.Friend;
import com.techtalk.entity.User;
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

    @Override
    @Transactional
    public Friend sendRequest(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 检查是否已存在好友关系
        Friend existing = friendMapper.findByUserAndFriend(userId, friendId);
        if (existing != null) {
            if ("ACCEPTED".equals(existing.getStatus())) {
                throw new RuntimeException("已经是好友了");
            }
            if ("PENDING".equals(existing.getStatus())) {
                throw new RuntimeException("已发送过好友请求，请等待对方处理");
            }
            // 如果之前被拒绝过，更新为 PENDING
            existing.setStatus("PENDING");
            existing.setUpdatedAt(LocalDateTime.now());
            updateById(existing);
            return existing;
        }

        // 检查反向是否已有请求（对方已经向我发送了请求）
        Friend reverse = friendMapper.findByUserAndFriend(friendId, userId);
        if (reverse != null && "PENDING".equals(reverse.getStatus())) {
            // 对方已经发过请求，直接接受
            reverse.setStatus("ACCEPTED");
            reverse.setUpdatedAt(LocalDateTime.now());
            updateById(reverse);
            return reverse;
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus("PENDING");
        friend.setCreatedAt(LocalDateTime.now());
        friend.setUpdatedAt(LocalDateTime.now());
        save(friend);
        return friend;
    }

    @Override
    @Transactional
    public void acceptRequest(Long requestId, Long userId) {
        Friend request = getById(requestId);
        if (request == null) throw new RuntimeException("请求不存在");
        if (!request.getFriendId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"PENDING".equals(request.getStatus())) throw new RuntimeException("请求已处理");

        request.setStatus("ACCEPTED");
        request.setUpdatedAt(LocalDateTime.now());
        updateById(request);

        // 创建双向好友关系
        Friend reverse = friendMapper.findByUserAndFriend(userId, request.getUserId());
        if (reverse == null) {
            Friend f2 = new Friend();
            f2.setUserId(userId);
            f2.setFriendId(request.getUserId());
            f2.setStatus("ACCEPTED");
            f2.setCreatedAt(LocalDateTime.now());
            f2.setUpdatedAt(LocalDateTime.now());
            save(f2);
        } else {
            reverse.setStatus("ACCEPTED");
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
        qw.eq("user_id", userId).eq("friend_id", friendId).eq("status", "ACCEPTED");
        Friend f1 = getOne(qw);
        if (f1 != null) {
            f1.setStatus("REJECTED");
            updateById(f1);
        }

        QueryWrapper<Friend> qw2 = new QueryWrapper<>();
        qw2.eq("user_id", friendId).eq("friend_id", userId).eq("status", "ACCEPTED");
        Friend f2 = getOne(qw2);
        if (f2 != null) {
            f2.setStatus("REJECTED");
            updateById(f2);
        }
    }

    @Override
    public List<Map<String, Object>> getFriends(Long userId) {
        List<Friend> friends = friendMapper.findFriendsByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend f : friends) {
            User friendUser = userMapper.selectById(f.getFriendId());
            if (friendUser == null) continue;
            Map<String, Object> info = buildUserInfo(friendUser);
            info.put("friendId", f.getId());
            info.put("since", f.getCreatedAt());
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
        return friendMapper.findFriendship(userId, friendId) != null;
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
