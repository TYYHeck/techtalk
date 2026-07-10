-- ============================================
-- TechTalk 增量迁移：聊天、好友、用户扩展字段
-- MySQL 5.7 compatible
-- ============================================
USE techtalk;

-- 添加用户扩展字段（使用存储过程处理列已存在的情况）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def VARCHAR(256)
)
BEGIN
    SET @col_exists = 0;
    SELECT COUNT(*) INTO @col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'techtalk'
      AND TABLE_NAME = tbl_name
      AND COLUMN_NAME = col_name;
    IF @col_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL add_column_if_not_exists('tt_user', 'nickname', "VARCHAR(50) DEFAULT '' COMMENT '昵称' AFTER bio");
CALL add_column_if_not_exists('tt_user', 'location', "VARCHAR(100) DEFAULT '' COMMENT '所在地' AFTER nickname");
CALL add_column_if_not_exists('tt_user', 'website', "VARCHAR(100) DEFAULT '' COMMENT '个人网站' AFTER location");
CALL add_column_if_not_exists('tt_user', 'github', "VARCHAR(100) DEFAULT '' COMMENT 'GitHub主页' AFTER website");

DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 好友关系表
CREATE TABLE IF NOT EXISTS tt_friend (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    friend_id   BIGINT       NOT NULL COMMENT '好友ID',
    status      VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态：PENDING/ACCEPTED/REJECTED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_friend (friend_id),
    UNIQUE KEY uk_user_friend (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS tt_chat_message (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id  VARCHAR(50)  NOT NULL COMMENT '会话ID',
    sender_id        BIGINT       NOT NULL COMMENT '发送者ID',
    receiver_id      BIGINT       NOT NULL COMMENT '接收者ID',
    content          TEXT         NOT NULL COMMENT '消息内容',
    is_read          TINYINT      DEFAULT 0 COMMENT '是否已读',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation (conversation_id),
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';
