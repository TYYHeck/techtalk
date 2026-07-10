package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtalk.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT f.* FROM tt_friend f WHERE f.user_id = #{userId} AND f.status = 'ACCEPTED'")
    List<Friend> findFriendsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM tt_friend WHERE user_id = #{userId} AND friend_id = #{friendId}")
    Friend findByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT * FROM tt_friend WHERE " +
            "((user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})) " +
            "AND status = 'ACCEPTED' LIMIT 1")
    Friend findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT * FROM tt_friend WHERE friend_id = #{userId} AND status = 'PENDING'")
    List<Friend> findPendingRequests(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM tt_friend WHERE user_id = #{userId} AND status = 'ACCEPTED'")
    int countFriends(@Param("userId") Long userId);
}
