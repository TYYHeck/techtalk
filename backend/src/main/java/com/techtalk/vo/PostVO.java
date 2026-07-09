package com.techtalk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Long categoryId;
    private String categoryName;

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
}
