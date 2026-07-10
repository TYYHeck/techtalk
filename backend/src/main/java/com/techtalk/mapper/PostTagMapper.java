package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtalk.entity.PostTag;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    /** 根据帖子ID获取所有分类 */
    @Select("SELECT pt.category_id, c.name AS category_name, c.icon AS category_icon " +
            "FROM tt_post_tag pt LEFT JOIN tt_category c ON pt.category_id = c.id " +
            "WHERE pt.post_id = #{postId}")
    List<Map<String, Object>> selectCategoriesByPostId(@Param("postId") Long postId);

    /** 批量获取多个帖子的分类 */
    @Select("<script>" +
            "SELECT pt.post_id, pt.category_id, c.name AS category_name, c.icon AS category_icon " +
            "FROM tt_post_tag pt LEFT JOIN tt_category c ON pt.category_id = c.id " +
            "WHERE pt.post_id IN " +
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectCategoriesByPostIds(@Param("postIds") List<Long> postIds);

    /** 删除帖子的所有标签 */
    @Delete("DELETE FROM tt_post_tag WHERE post_id = #{postId}")
    void deleteByPostId(@Param("postId") Long postId);

    /** 根据分类ID删除关联（删除分类时用） */
    @Delete("DELETE FROM tt_post_tag WHERE category_id = #{categoryId}")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
}
