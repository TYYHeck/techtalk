package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("<script>" +
            "SELECT p.*, u.username AS author_username, u.avatar AS author_avatar, " +
            "c.name AS category_name " +
            "FROM tt_post p " +
            "LEFT JOIN tt_user u ON p.user_id = u.id " +
            "LEFT JOIN tt_category c ON p.category_id = c.id " +
            "WHERE p.deleted = 0 AND p.status = 'PUBLISHED' " +
            "<if test='categoryId != null'>AND p.category_id = #{categoryId}</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.summary LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY p.is_pinned DESC, p.is_featured DESC, p.created_at DESC" +
            "</script>")
    @Results({
            @Result(column = "author_username", property = "authorUsername"),
            @Result(column = "author_avatar", property = "authorAvatar"),
            @Result(column = "category_name", property = "categoryName")
    })
    Page<Map<String, Object>> selectPublishedPage(Page<Post> page,
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("keyword") String keyword);

    @Select("SELECT * FROM tt_post WHERE id = #{id} AND deleted = 0")
    Post selectByIdWithDeleted(@Param("id") Long id);

    @Update("UPDATE tt_post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE tt_post SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE tt_post SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(@Param("id") Long id);

    @Update("UPDATE tt_post SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(@Param("id") Long id);

    @Update("UPDATE tt_post SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    void decrementCommentCount(@Param("id") Long id);

    @Update("UPDATE tt_post SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    void incrementFavoriteCount(@Param("id") Long id);

    @Update("UPDATE tt_post SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE id = #{id}")
    void decrementFavoriteCount(@Param("id") Long id);

    // === 管理端 ===

    @Select("<script>" +
            "SELECT * FROM tt_post WHERE deleted = 0 " +
            "<if test='status != null'>AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND title LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    Page<Post> selectAdminPage(Page<Post> page,
                                @Param("status") String status,
                                @Param("keyword") String keyword);

    /** 近7天每日发帖趋势 */
    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS count FROM tt_post " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> selectWeeklyPostTrend();

    /** 统计总数 */
    @Select("SELECT COUNT(*) FROM tt_post WHERE deleted = 0")
    Long countPosts();

    @Select("SELECT COUNT(*) FROM tt_post WHERE deleted = 0 AND DATE(created_at) = CURDATE()")
    Long countTodayPosts();
}
