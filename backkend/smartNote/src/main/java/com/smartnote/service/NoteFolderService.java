package com.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.dto.NoteFolderCreateDTO;
import com.smartnote.dto.NoteFolderRenameDTO;
import com.smartnote.pojo.NoteFolder;
import com.smartnote.vo.NoteFolderVO;

import java.util.List;

public interface NoteFolderService extends IService<NoteFolder> {
    
    /**
     * 创建文件夹
     */
    Long createFolder(NoteFolderCreateDTO dto, Long userId);
    
    /**
     * 重命名文件夹
     */
    void renameFolder(Long folderId, NoteFolderRenameDTO dto, Long userId);
    
    /**
     * 删除文件夹（将笔记移动到默认文件夹）
     */
    void deleteFolder(Long folderId, Long userId);
    
    /**
     * 获取用户的文件夹列表（仅一级，无嵌套）
     */
    List<NoteFolderVO> getFolderTree(Long userId);
    
    /**
     * 初始化用户默认文件夹
     */
    void initDefaultFolder(Long userId);
}
