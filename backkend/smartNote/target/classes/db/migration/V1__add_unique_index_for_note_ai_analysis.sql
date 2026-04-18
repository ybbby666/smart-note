-- 为 note_ai_analysis 表添加唯一索引，保证 (note_id, user_id) 的唯一性
-- 这样可以防止并发请求产生重复数据

ALTER TABLE note_ai_analysis 
ADD UNIQUE INDEX uk_note_user (note_id, user_id);
