package com.techtalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发帖 / 编辑帖子请求
 */
@Data
public class PostDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度 2-100 位")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 50000, message = "内容长度 10-50000 位")
    private String content;

    /** 纯文本摘要（前端可传，后端也可自动生成） */
    private String summary;

    @NotNull(message = "分类不能为空")
    private Long categoryId;
}
