package com.techtalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techtalk.entity.Friend;

import java.util.List;
import java.util.Map;

public interface FriendService extends IService<Friend> {

    /** 关注用户（直接关注，无需对方同意） */
    void follow(Long userId, Long targetId);

    /** 取消关注 */
    void unfollow(Long userId, Long targetId);

    /** 接受好友请求（兼容旧逻辑） */
    void acceptRequest(Long requestId, Long userId);

    /** 拒绝好友请求（兼容旧逻辑） */
    void rejectRequest(Long requestId, Long userId);

    /** 移除好友/取消互关 */
    void removeFriend(Long userId, Long friendId);

    /** 获取好友列表（互关用户） */
    List<Map<String, Object>> getFriends(Long userId);

    /** 获取关注列表 */
    List<Map<String, Object>> getFollowing(Long userId);

    /** 获取粉丝列表 */
    List<Map<String, Object>> getFollowers(Long userId);

    /** 获取待处理的好友请求（兼容） */
    List<Map<String, Object>> getPendingRequests(Long userId);

    /** 检查是否为互关好友 */
    boolean isFriend(Long userId, Long friendId);

    /** 检查是否关注了对方 */
    boolean isFollowing(Long userId, Long targetId);

    /** 检查是否可以发私信 */
    boolean canSendMessage(Long senderId, Long receiverId);
}
