package com.techtalk.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private Long total;
    private Long page;
    private Long size;
    private Long pages;

    public static <T> PageResult<T> of(List<T> records, Long total, Long page, Long size) {
        long pages = (total + size - 1) / size;
        return new PageResult<>(records, total, page, size, pages);
    }
}
