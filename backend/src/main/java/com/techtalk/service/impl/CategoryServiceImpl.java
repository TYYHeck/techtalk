package com.techtalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtalk.common.BusinessException;
import com.techtalk.entity.Category;
import com.techtalk.mapper.CategoryMapper;
import com.techtalk.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> getActiveCategories() {
        return categoryMapper.selectActiveCategories();
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public Category createCategory(String name, String description, String icon) {
        // 名称唯一性
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setSortOrder(0);
        category.setPostCount(0);
        category.setStatus("ACTIVE");

        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Long id, String name, String description,
                                    String icon, Integer sortOrder) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        if (icon != null) category.setIcon(icon);
        if (sortOrder != null) category.setSortOrder(sortOrder);

        categoryMapper.updateById(category);
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (category.getPostCount() > 0) {
            throw new BusinessException("该分类下还有帖子，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
