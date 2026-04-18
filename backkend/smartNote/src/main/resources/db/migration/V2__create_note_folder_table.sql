-- 创建笔记文件夹表（仅支持一级文件夹，无多层嵌套）
CREATE TABLE IF NOT EXISTS `note_folder` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件夹ID',
    `name` VARCHAR(50) NOT NULL COMMENT '文件夹名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父文件夹ID，始终为NULL（仅支持一级文件夹）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记文件夹表';

-- 为 note 表添加 folder_id 字段（null时自动分配到默认文件夹）
ALTER TABLE `note` ADD COLUMN `folder_id` BIGINT DEFAULT NULL COMMENT '文件夹ID' AFTER `user_id`;
ALTER TABLE `note` ADD INDEX `idx_folder_id` (`folder_id`);
