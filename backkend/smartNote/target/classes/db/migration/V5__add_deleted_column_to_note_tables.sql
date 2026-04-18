-- 为 note 表添加 deleted 字段（逻辑删除）
ALTER TABLE `note` ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 为 note_folder 表添加 deleted 字段（如果不存在）
ALTER TABLE `note_folder` ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';
