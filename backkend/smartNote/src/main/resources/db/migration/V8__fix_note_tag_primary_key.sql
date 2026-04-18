-- 修复 note_tag 表的主键和自增问题
-- 将 tag_id 改名为 id,并设置为主键自增

ALTER TABLE note_tag CHANGE COLUMN tag_id id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE note_tag ADD PRIMARY KEY (id);
