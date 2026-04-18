-- 创建笔记批注表
CREATE TABLE IF NOT EXISTS `note_annotation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '批注ID',
    `note_id` BIGINT NOT NULL COMMENT '笔记ID',
    `content` TEXT NOT NULL COMMENT '批注内容',
    `position` VARCHAR(200) DEFAULT NULL COMMENT '批注位置（可选）',
    `user_id` BIGINT NOT NULL COMMENT '批注人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_note_id` (`note_id`),
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`note_id`) REFERENCES `note`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记批注表';
