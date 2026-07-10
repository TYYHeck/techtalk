package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtalk.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    /** 查某用户关注的人（status IN FOLLOWING/ACCEPTED 兼容旧数据） */
    @Select("SELECT f.* FROM tt_friend f WHERE f.user_id = #{userId} AND f.status IN ('FOLLOWING','ACCEPTED')")
    List<Friend> findFollowingByUserId(@Param("userId") Long userId);

    /** 查某用户的粉丝（关注了该用户的人） */
    @Select("SELECT f.* FROM tt_friend f WHERE f.friend_id = #{userId} AND f.status IN ('FOLLOWING','ACCEPTED')")
    List<Friend> findFollowersByUserId(@Param("userId") Long userId);

    /** 查某用户的好友列表（互相关注，即双向 FOLLOWING） */
    @Select("SELECT f.* FROM tt_friend f WHERE f.user_id = #{userId} AND f.status IN ('FOLLOWING','ACCEPTED') " +
            "AND EXISTS (SELECT 1 FROM tt_friend f2 WHERE f2.user_id = f.friend_id AND f2.friend_id = f.user_id AND f2.status IN ('FOLLOWING','ACCEPTED'))")
    List<Friend> findMutualFriendsByUserId(@Param("userId") Long userId);

    /** 查两个用户之间的任意关系记录 */
    @Select("SELECT * FROM tt_friend WHERE user_id = #{userId} AND friend_id = #{friendId}")
    Friend findByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 查双向互关关系（任意方向 ACCEPTED 或 FOLLOWING 均视为互关） */
    @Select("SELECT * FROM tt_friend WHERE " +
            "((user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})) " +
            "AND status IN ('FOLLOWING','ACCEPTED') LIMIT 1")
    Friend findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 判断是否互关（双方都有 FOLLOWING/ACCEPTED 记录） */
    @Select("SELECT COUNT(*) = 2 FROM tt_friend WHERE " +
            "((user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})) " +
            "AND status IN ('FOLLOWING','ACCEPTED')")
    boolean isMutualFollowing(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 查发给某用户的待处理请求（PENDING 状态，friend_id 为接收者） */
    @Select("SELECT * FROM tt_friend WHERE friend_id = #{userId} AND status = 'PENDING'")
    List<Friend> findPendingRequests(@Param("userId") Long userId);

    /** 查某用户是否关注了另一个用户 */
    @Select("SELECT COUNT(*) > 0 FROM tt_friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND status IN ('FOLLOWING','ACCEPTED')")
    boolean isFollowing(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 统计关注数 */
    @Select("SELECT COUNT(*) FROM tt_friend WHERE user_id = #{userId} AND status IN ('FOLLOWING','ACCEPTED')")
    int countFollowing(@Param("userId") Long userId);

    /** 统计粉丝数 */
    @Select("SELECT COUNT(*) FROM tt_friend WHERE friend_id = #{userId} AND status IN ('FOLLOWING','ACCEPTED')")
    int countFollowers(@Param("userId") Long userId);
}
