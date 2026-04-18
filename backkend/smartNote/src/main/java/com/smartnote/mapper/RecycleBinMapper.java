package com.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartnote.pojo.RecycleBin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecycleBinMapper extends BaseMapper<RecycleBin> {
    
    /**
     * 物理删除笔记（绕过逻辑删除）
     */
    @Delete("DELETE FROM note WHERE id = #{noteId}")
    int physicallyDeleteNote(Long noteId);
    
    /**
     * 物理删除文件夹（绕过逻辑删除）
     */
    @Delete("DELETE FROM note_folder WHERE id = #{folderId}")
    int physicallyDeleteFolder(Long folderId);
}
