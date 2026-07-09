package com.techtalk.dto;

import lombok.Data;

/**
 * 通用分页查询请求
 */
@Data
public class PageQueryDTO {

    private Long page = 1L;
    private Long size = 10L;
    private String keyword;
    private String orderBy;
    private Long categoryId;
}
