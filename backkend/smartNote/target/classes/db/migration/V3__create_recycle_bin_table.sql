-- 创建回收站表
CREATE TABLE IF NOT EXISTS `recycle_bin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '回收站记录ID',
    `resource_type` VARCHAR(20) NOT NULL COMMENT '资源类型：note-笔记, folder-文件夹',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `delete_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '删除时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间（30天后）',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expire_time` (`expire_time`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回收站表';
