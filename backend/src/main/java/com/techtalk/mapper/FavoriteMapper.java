package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.util.Set;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT COUNT(*) FROM tt_favorite WHERE user_id = #{userId} AND post_id = #{postId}")
    int countByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT id FROM tt_favorite WHERE user_id = #{userId} AND post_id = #{postId} LIMIT 1")
    Favorite selectByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT post_id FROM tt_favorite WHERE user_id = #{userId}")
    Set<Long> selectFavoritePostIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT f.id, f.created_at AS favorited_at, p.*, u.username AS author_username, u.avatar AS author_avatar " +
            "FROM tt_favorite f " +
            "LEFT JOIN tt_post p ON f.post_id = p.id " +
            "LEFT JOIN tt_user u ON p.user_id = u.id " +
            "WHERE f.user_id = #{userId} AND p.deleted = 0 " +
            "ORDER BY f.created_at DESC")
    Page<Map<String, Object>> selectUserFavorites(Page<Favorite> page, @Param("userId") Long userId);
}
