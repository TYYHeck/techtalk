package com.techtalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techtalk.entity.Friend;

import java.util.List;
import java.util.Map;

public interface FriendService extends IService<Friend> {

    /** 发送好友请求 */
    Friend sendRequest(Long userId, Long friendId);

    /** 接受好友请求 */
    void acceptRequest(Long requestId, Long userId);

    /** 拒绝好友请求 */
    void rejectRequest(Long requestId, Long userId);

    /** 删除好友 */
    void removeFriend(Long userId, Long friendId);

    /** 获取好友列表 */
    List<Map<String, Object>> getFriends(Long userId);

    /** 获取待处理的好友请求 */
    List<Map<String, Object>> getPendingRequests(Long userId);

    /** 检查是否为好友 */
    boolean isFriend(Long userId, Long friendId);
}
