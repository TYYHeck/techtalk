package com.techtalk.service;

import com.techtalk.entity.Category;

import java.util.List;

/**
 * 分类服务
 */
public interface CategoryService {

    List<Category> getActiveCategories();

    Category getById(Long id);

    List<Category> getAllCategories();

    Category createCategory(String name, String description, String icon);

    Category updateCategory(Long id, String name, String description, String icon, Integer sortOrder);

    void deleteCategory(Long id);
}
