package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtalk.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM tt_category WHERE status = 'ACTIVE' ORDER BY sort_order ASC")
    List<Category> selectActiveCategories();

    @Update("UPDATE tt_category SET post_count = post_count + 1 WHERE id = #{categoryId}")
    void incrementPostCount(@Param("categoryId") Long categoryId);

    @Update("UPDATE tt_category SET post_count = GREATEST(post_count - 1, 0) WHERE id = #{categoryId}")
    void decrementPostCount(@Param("categoryId") Long categoryId);
}
