package com.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartnote.pojo.NoteFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoteFolderMapper extends BaseMapper<NoteFolder> {
    
    /**
     * 恢复文件夹（绕过逻辑删除）
     */
    @Update("UPDATE note_folder SET deleted = 0 WHERE id = #{id}")
    int restoreFolder(Long id);
}
