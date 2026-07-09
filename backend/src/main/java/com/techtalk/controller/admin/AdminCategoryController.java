package com.techtalk.controller.admin;

import com.techtalk.common.Result;
import com.techtalk.entity.Category;
import com.techtalk.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端 - 分类管理
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    public Result<Category> create(@RequestBody Map<String, String> body) {
        Category category = categoryService.createCategory(
                body.get("name"), body.get("description"), body.get("icon"));
        return Result.ok(category);
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Category category = categoryService.updateCategory(
                id,
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("icon"),
                body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null);
        return Result.ok(category);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.ok();
    }
}
