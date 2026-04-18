-- 为回收站表添加 resource_name 字段
ALTER TABLE recycle_bin ADD COLUMN resource_name VARCHAR(255) COMMENT '资源名称（笔记标题或文件夹名称）';
