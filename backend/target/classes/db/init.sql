-- ============================================
-- TechTalk 论坛数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS techtalk
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE techtalk;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    email       VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    avatar      VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    bio         VARCHAR(500) DEFAULT '' COMMENT '个性签名',
    role        VARCHAR(20)  DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status      VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/BANNED',
    post_count  INT          DEFAULT 0 COMMENT '发帖数',
    like_count  INT          DEFAULT 0 COMMENT '获赞数',
    last_login_ip    VARCHAR(50)  DEFAULT '' COMMENT '最后登录IP',
    last_login_time  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 分类表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE COMMENT '分类名',
    description VARCHAR(200) DEFAULT '' COMMENT '描述',
    icon        VARCHAR(100) DEFAULT '📁' COMMENT '图标',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    post_count  INT          DEFAULT 0 COMMENT '帖子数',
    status      VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================
-- 帖子表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_post (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(200) NOT NULL COMMENT '标题',
    content        LONGTEXT     NOT NULL COMMENT '内容（富文本）',
    summary        VARCHAR(500) DEFAULT '' COMMENT '摘要',
    category_id    BIGINT       NOT NULL COMMENT '分类ID',
    user_id        BIGINT       NOT NULL COMMENT '作者ID',
    view_count     INT          DEFAULT 0 COMMENT '浏览数',
    like_count     INT          DEFAULT 0 COMMENT '点赞数',
    comment_count  INT          DEFAULT 0 COMMENT '评论数',
    favorite_count INT          DEFAULT 0 COMMENT '收藏数',
    is_pinned      TINYINT      DEFAULT 0 COMMENT '是否置顶',
    is_featured    TINYINT      DEFAULT 0 COMMENT '是否精华',
    status         VARCHAR(20)  DEFAULT 'PUBLISHED' COMMENT '状态：PUBLISHED/DRAFT/AUDITING/REJECTED',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category (category_id),
    INDEX idx_user (user_id),
    INDEX idx_created (created_at),
    FULLTEXT INDEX ft_title_summary (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ============================================
-- 评论表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_comment (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    content          TEXT         NOT NULL COMMENT '内容',
    post_id          BIGINT       NOT NULL COMMENT '帖子ID',
    user_id          BIGINT       NOT NULL COMMENT '评论用户ID',
    parent_id        BIGINT       DEFAULT 0 COMMENT '父评论ID',
    reply_to_user_id BIGINT       DEFAULT NULL COMMENT '回复目标用户ID',
    like_count       INT          DEFAULT 0 COMMENT '点赞数',
    status           VARCHAR(20)  DEFAULT 'PUBLISHED' COMMENT '状态',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted          TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 点赞记录表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_like_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型：POST/COMMENT',
    target_id   BIGINT      NOT NULL COMMENT '目标ID',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- ============================================
-- 收藏表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_favorite (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT   NOT NULL COMMENT '用户ID',
    post_id    BIGINT   NOT NULL COMMENT '帖子ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 通知表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_notification (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '接收用户ID',
    from_user_id BIGINT      DEFAULT NULL COMMENT '触发用户ID',
    type        VARCHAR(30)  NOT NULL COMMENT '通知类型：LIKE/COMMENT/REPLY/SYSTEM',
    title       VARCHAR(200) NOT NULL COMMENT '标题',
    content     VARCHAR(500) DEFAULT '' COMMENT '内容',
    post_id     BIGINT       DEFAULT NULL COMMENT '关联帖子ID',
    comment_id  BIGINT       DEFAULT NULL COMMENT '关联评论ID',
    is_read     TINYINT      DEFAULT 0 COMMENT '是否已读',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================
-- 操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS tt_operation_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       DEFAULT NULL COMMENT '操作人ID',
    username   VARCHAR(50)  DEFAULT '' COMMENT '操作人用户名',
    action     VARCHAR(100) DEFAULT '' COMMENT '操作类型',
    target     VARCHAR(200) DEFAULT '' COMMENT '操作目标',
    detail     TEXT         DEFAULT NULL COMMENT '操作详情',
    ip         VARCHAR(50)  DEFAULT '' COMMENT '请求IP',
    uri        VARCHAR(200) DEFAULT '' COMMENT '请求URI',
    method     VARCHAR(10)  DEFAULT '' COMMENT '请求方法',
    time_cost  BIGINT       DEFAULT 0 COMMENT '耗时(ms)',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 初始数据：默认分类 & 管理员
-- ============================================
INSERT INTO tt_category (name, description, icon, sort_order) VALUES
('综合讨论', '技术相关话题的综合讨论区', '💬', 1),
('前端开发', 'HTML/CSS/JavaScript/Vue/React 等前端技术', '🎨', 2),
('后端开发', 'Java/Python/Go 等后端技术、架构设计', '⚙️', 3),
('AI & 大模型', '人工智能、LLM、Agent 开发相关', '🤖', 4),
('开源项目', '开源项目推荐、经验分享', '📦', 5),
('求职面试', '面试经验、简历指导、职场交流', '💼', 6);

-- 默认管理员账号: admin / admin123
INSERT INTO tt_user (username, password, email, role, status, avatar) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@techtalk.com', 'ADMIN', 'ACTIVE', 'https://api.dicebear.com/7.x/initials/svg?seed=Admin');
