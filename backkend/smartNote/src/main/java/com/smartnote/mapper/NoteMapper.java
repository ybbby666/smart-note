package com.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartnote.pojo.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    
    /**
     * 恢复笔记（绕过逻辑删除）但MyBatis-Plus在执行UPDATE之前,会先检查记录是否存在,而这个检查会自动添加 deleted=0 的条件
     */
    @Update("UPDATE note SET deleted = 0 WHERE id = #{id}")
    int restoreNote(Long id);
}
