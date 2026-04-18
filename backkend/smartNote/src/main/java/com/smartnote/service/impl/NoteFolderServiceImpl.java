package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.dto.NoteFolderCreateDTO;
import com.smartnote.dto.NoteFolderRenameDTO;
import com.smartnote.exception.BusinessException;
import com.smartnote.mapper.NoteFolderMapper;
import com.smartnote.mapper.NoteMapper;
import com.smartnote.pojo.Note;
import com.smartnote.pojo.NoteFolder;
import com.smartnote.service.NoteFolderService;
import com.smartnote.service.RecycleBinService;
import com.smartnote.vo.NoteFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoteFolderServiceImpl extends ServiceImpl<NoteFolderMapper, NoteFolder> implements NoteFolderService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private RecycleBinService recycleBinService;
    @Override
    @Transactional
    public Long createFolder(NoteFolderCreateDTO dto, Long userId) {
        log.info("创建文件夹：名称={}, 父ID={}, 用户ID={}", dto.getName(), dto.getParentId(), userId);
        // 只允许创建一级文件夹，不支持嵌套
        if (dto.getParentId() != null) {
            throw new BusinessException("仅支持一级文件夹，不允许创建子文件夹");
        }
        NoteFolder folder = new NoteFolder();
        folder.setName(dto.getName());
        folder.setParentId(null); // 强制设置为根目录
        folder.setUserId(userId);
        folder.setSortOrder(0);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        save(folder);
        log.info("文件夹创建成功：ID={}", folder.getId());
        return folder.getId();
    }

    @Override
    @Transactional
    public void renameFolder(Long folderId, NoteFolderRenameDTO dto, Long userId) {
        log.info("重命名文件夹：ID={}, 新名称={}", folderId, dto.getName());
        NoteFolder folder = getById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException("文件夹不存在");
        }
        folder.setName(dto.getName());
        folder.setUpdateTime(LocalDateTime.now());
        updateById(folder);
        log.info("文件夹重命名成功");
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId, Long userId) {
        log.info("删除文件夹：ID={}", folderId);
        NoteFolder folder = getById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException("文件夹不存在");
        }
        // 不允许删除默认文件夹（第一个创建的文件夹）
        LambdaQueryWrapper<NoteFolder> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.eq(NoteFolder::getUserId, userId)
                     .orderByAsc(NoteFolder::getCreateTime)
                     .orderByAsc(NoteFolder::getId); // 添加ID作为第二排序条件
        
        // 使用 list() 避免 TooManyResultsException
        List<NoteFolder> folderList = list(defaultWrapper);
        NoteFolder defaultFolder = (folderList != null && !folderList.isEmpty()) ? folderList.get(0) : null;
        if (defaultFolder != null && folder.getId().equals(defaultFolder.getId())) {
            throw new BusinessException("不能删除默认文件夹");
        }
        // 将该文件夹下的笔记移动到默认文件夹
        Long defaultFolderId = getDefaultFolderId(userId);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getFolderId, folderId);
        List<Note> notes = noteMapper.selectList(wrapper);
        for (Note note : notes) {
            note.setFolderId(defaultFolderId);
            noteMapper.updateById(note);
        }
        folder.setDeleted(1);
        updateById(folder);
        recycleBinService.addToRecycleBin("folder", folderId, userId);
        log.info("文件夹已移至回收站：ID={}", folderId);
    }
    
    /**
     * 获取用户的默认文件夹ID
     */
    private Long getDefaultFolderId(Long userId) {
        LambdaQueryWrapper<NoteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteFolder::getUserId, userId)
               .orderByAsc(NoteFolder::getCreateTime)
               .orderByAsc(NoteFolder::getId); // 添加ID作为第二排序条件，确保唯一性
        
        // 使用 list() 避免 TooManyResultsException
        List<NoteFolder> folderList = list(wrapper);
        if (folderList != null && !folderList.isEmpty()) {
            return folderList.get(0).getId();
        }
        return null;
    }
    
    @Override
    public List<NoteFolderVO> getFolderTree(Long userId) {
        log.info("获取文件夹列表：用户ID={}", userId);
        // 查询用户的所有文件夹（只有一级，无嵌套）
        LambdaQueryWrapper<NoteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteFolder::getUserId, userId)
               .orderByAsc(NoteFolder::getSortOrder)
               .orderByDesc(NoteFolder::getCreateTime);
        List<NoteFolder> folders = list(wrapper);
        // 转换为VO
        return folders.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void initDefaultFolder(Long userId) {
        log.info("初始化用户默认文件夹：用户ID={}", userId);
        // 检查是否已有文件夹
        LambdaQueryWrapper<NoteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteFolder::getUserId, userId);
        long count = count(wrapper);
        if (count == 0) {
            NoteFolder defaultFolder = new NoteFolder();
            defaultFolder.setName("全部笔记");
            defaultFolder.setParentId(null);
            defaultFolder.setUserId(userId);
            defaultFolder.setSortOrder(0);
            defaultFolder.setCreateTime(LocalDateTime.now());
            defaultFolder.setUpdateTime(LocalDateTime.now());
            save(defaultFolder);
            log.info("默认文件夹创建成功：ID={}", defaultFolder.getId());
        }
    }
    private NoteFolderVO convertToVO(NoteFolder folder) {
        NoteFolderVO vo = new NoteFolderVO();
        vo.setId(folder.getId());
        vo.setName(folder.getName());
        vo.setParentId(folder.getParentId());
        vo.setSortOrder(folder.getSortOrder());
        vo.setCreateTime(folder.getCreateTime());
        // 统计文件夹下的笔记数量
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getFolderId, folder.getId());
        vo.setNoteCount(Math.toIntExact(noteMapper.selectCount(wrapper)));
        return vo;
    }
}
