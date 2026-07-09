package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtalk.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    @Select("SELECT COUNT(*) FROM tt_like_record WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int countByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") String targetType,
                              @Param("targetId") Long targetId);

    @Select("SELECT id FROM tt_like_record WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} LIMIT 1")
    LikeRecord selectByUserAndTarget(@Param("userId") Long userId,
                                      @Param("targetType") String targetType,
                                      @Param("targetId") Long targetId);

    @Select("SELECT target_id FROM tt_like_record WHERE user_id = #{userId} AND target_type = 'POST'")
    java.util.Set<Long> selectLikedPostIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT target_id FROM tt_like_record WHERE user_id = #{userId} AND target_type = 'COMMENT'")
    java.util.Set<Long> selectLikedCommentIdsByUserId(@Param("userId") Long userId);
}
