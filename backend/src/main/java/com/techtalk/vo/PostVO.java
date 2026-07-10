package com.techtalk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表/详情视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVO {

    private Long id;
    private String title;
    private String content;
    private String summary;

    /** 主分类ID（兼容旧字段） */
    private Long categoryId;
    /** 主分类名称（兼容旧字段） */
    private String categoryName;

    /** 所有关联的分类标签 */
    private List<CategoryTagVO> categories;

    /** 作者信息 */
    private UserVO author;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Boolean isPinned;
    private Boolean isFeatured;
    private String status;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;
    /** 当前用户是否已收藏 */
    private Boolean isFavorited;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 分类标签视图（用于帖子列表/详情展示）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTagVO {
        private Long id;
        private String name;
        private String icon;
    }
}
