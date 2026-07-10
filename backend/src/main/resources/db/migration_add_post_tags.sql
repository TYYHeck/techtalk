-- ============================================
-- 帖子多分类标签表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_post_tag (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL COMMENT '帖子ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    INDEX idx_post (post_id),
    INDEX idx_category (category_id),
    UNIQUE KEY uk_post_category (post_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子-分类关联表';

-- 迁移现有数据：把 tt_post.category_id 写入 tt_post_tag
INSERT IGNORE INTO tt_post_tag (post_id, category_id)
SELECT id, category_id FROM tt_post WHERE category_id IS NOT NULL AND category_id > 0;
