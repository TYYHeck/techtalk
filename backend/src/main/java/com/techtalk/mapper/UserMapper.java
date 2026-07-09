package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM tt_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM tt_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    @Update("UPDATE tt_user SET post_count = post_count + 1 WHERE id = #{userId}")
    void incrementPostCount(@Param("userId") Long userId);

    @Update("UPDATE tt_user SET post_count = GREATEST(post_count - 1, 0) WHERE id = #{userId}")
    void decrementPostCount(@Param("userId") Long userId);

    @Update("UPDATE tt_user SET like_count = like_count + 1 WHERE id = #{userId}")
    void incrementLikeCount(@Param("userId") Long userId);

    // 分页查询用户列表（管理端用）
    @Select("<script>" +
            "SELECT * FROM tt_user WHERE deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    Page<User> selectPageWithKeyword(Page<User> page, @Param("keyword") String keyword);
}
