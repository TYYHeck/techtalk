package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.username AS user_username, u.avatar AS user_avatar, " +
            "ru.username AS reply_username " +
            "FROM tt_comment c " +
            "LEFT JOIN tt_user u ON c.user_id = u.id " +
            "LEFT JOIN tt_user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.post_id = #{postId} AND c.parent_id = 0 AND c.deleted = 0 " +
            "ORDER BY c.created_at DESC")
    @Results({
            @Result(column = "user_username", property = "userUsername"),
            @Result(column = "user_avatar", property = "userAvatar"),
            @Result(column = "reply_username", property = "replyUsername")
    })
    List<Map<String, Object>> selectTopLevelByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, u.username AS user_username, u.avatar AS user_avatar, " +
            "ru.username AS reply_username " +
            "FROM tt_comment c " +
            "LEFT JOIN tt_user u ON c.user_id = u.id " +
            "LEFT JOIN tt_user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.parent_id = #{parentId} AND c.deleted = 0 " +
            "ORDER BY c.created_at ASC")
    @Results({
            @Result(column = "user_username", property = "userUsername"),
            @Result(column = "user_avatar", property = "userAvatar"),
            @Result(column = "reply_username", property = "replyUsername")
    })
    List<Map<String, Object>> selectByParentId(@Param("parentId") Long parentId);

    @Update("UPDATE tt_comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE tt_comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(@Param("id") Long id);

    // === 管理端 ===
    @Select("<script>" +
            "SELECT c.*, u.username AS user_username, p.title AS post_title " +
            "FROM tt_comment c " +
            "LEFT JOIN tt_user u ON c.user_id = u.id " +
            "LEFT JOIN tt_post p ON c.post_id = p.id " +
            "WHERE c.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND c.content LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            "ORDER BY c.created_at DESC" +
            "</script>")
    @Results({
            @Result(column = "user_username", property = "userUsername"),
            @Result(column = "post_title", property = "postTitle")
    })
    Page<Map<String, Object>> selectAdminPage(Page<Comment> page, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM tt_comment WHERE deleted = 0")
    Long countComments();

    @Select("SELECT COUNT(*) FROM tt_comment WHERE deleted = 0 AND DATE(created_at) = CURDATE()")
    Long countTodayComments();
}
