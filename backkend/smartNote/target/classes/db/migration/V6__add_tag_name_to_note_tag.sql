-- 为 note_tag 表添加 tag_name 字段
ALTER TABLE `note_tag` ADD COLUMN `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称' AFTER `note_id`;
