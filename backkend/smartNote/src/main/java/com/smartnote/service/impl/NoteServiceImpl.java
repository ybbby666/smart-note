package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.constants.NoteConstant;
import com.smartnote.constants.NotePermissionConstant;
import com.smartnote.mapper.*;
import com.smartnote.pojo.*;
import com.smartnote.service.NoteFolderService;
import com.smartnote.service.NoteService;
import com.smartnote.service.RecycleBinService;
import com.smartnote.vo.NoteVO;
import com.smartnote.dto.NoteAddDTO;
import com.smartnote.dto.NoteUpdateDTO;
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
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteTagMapper noteTagMapper;
    @Autowired
    private NoteHistoryMapper noteHistoryMapper;
    @Autowired
    private NotePermissionMapper notePermissionMapper;
    @Autowired
    private NoteShareUserMapper noteShareUserMapper;
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RecycleBinService recycleBinService;
    
    @Autowired
    private NoteFolderService noteFolderService;
    private NoteVO toVO(Note note) {
        NoteVO vo = new NoteVO();
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setCreateTime(note.getCreateTime());
        vo.setUpdateTime(note.getUpdateTime());
        vo.setFolderId(note.getFolderId());
        vo.setCreatorId(note.getUserId());
        User creator = userMapper.selectById(note.getUserId());
        if (creator != null) {
            vo.setCreatorName(creator.getNickname() != null ? creator.getNickname() : creator.getUsername());
        }
        List<String> tags = noteTagMapper.selectList(
                        new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, note.getId()))
                .stream()
                .map(NoteTag::getTagName)
                .collect(Collectors.toList());
        vo.setTags(tags);
        return vo;
    }
    @Transactional
    @Override
    public Long addNote(NoteAddDTO noteAddDTO, Long userId) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(noteAddDTO.getTitle());
        note.setContent(noteAddDTO.getContent());
        // 如果没有指定文件夹，自动分配到默认文件夹
        if (noteAddDTO.getFolderId() == null) {
            Long defaultFolderId = getDefaultFolderId(userId);
            note.setFolderId(defaultFolderId);
            log.info("创建笔记时未指定文件夹，自动分配到默认文件夹: {}", defaultFolderId);
        } else {
            note.setFolderId(noteAddDTO.getFolderId());
        }
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        note.setDeleted(NoteConstant.NOT_DELETED);
        noteMapper.insert(note);
        if(noteAddDTO.getTags()!=null&&noteAddDTO.getTags().size()>0){
            for(String tagName : noteAddDTO.getTags()){
                NoteTag noteTag = new NoteTag();
                noteTag.setNoteId(note.getId());
                noteTag.setTagName(tagName);
                noteTagMapper.insert(noteTag);
            }
        }
        return note.getId();
    }
    @Override
    public Object page(Integer pageNum, Integer pageSize, String keyword, String tag, Long folderId, Long userId) {
        log.info("开始查询笔记列表: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        LambdaQueryWrapper<Note> myNoteWrapper = new LambdaQueryWrapper<>();
        myNoteWrapper.eq(Note::getDeleted, NoteConstant.NOT_DELETED);
        myNoteWrapper.eq(Note::getUserId, userId);
        //处理文件夹
        if (folderId != null) {
            if (folderId == 0) {
                myNoteWrapper.isNull(Note::getFolderId);
            } else {
                myNoteWrapper.eq(Note::getFolderId, folderId);
            }
        }
        List<Long> myNoteIds = noteMapper.selectList(myNoteWrapper)
                .stream()
                .map(Note::getId)
                .toList();
        log.info("查询到自己创建的笔记数量: {}", myNoteIds.size());
        List<Long> sharedNoteIds = new ArrayList<>();
        //查询公开的笔记
        LambdaQueryWrapper<NotePermission> publicPermWrapper = new LambdaQueryWrapper<>();
        publicPermWrapper.eq(NotePermission::getPermissionType, NotePermissionConstant.TYPE_PUBLIC);
        List<Long> publicNoteIds = notePermissionMapper.selectList(publicPermWrapper)
                .stream()
                .map(NotePermission::getNoteId)
                .toList();
        log.info("查询到公开笔记数量: {}", publicNoteIds.size());
        sharedNoteIds.addAll(publicNoteIds);
        //查询好友共享的笔记
        LambdaQueryWrapper<NoteShareUser> shareUserWrapper = new LambdaQueryWrapper<>();
        shareUserWrapper.eq(NoteShareUser::getUserId, userId);
        List<NoteShareUser> shareUserList = noteShareUserMapper.selectList(shareUserWrapper);
        log.info("查询到共享用户记录数量: {}", shareUserList.size());
        if (!shareUserList.isEmpty()) {
            log.info("共享记录详情: {}", shareUserList);
        }
        List<Long> friendSharedNoteIds = shareUserList.stream()
                .map(NoteShareUser::getNoteId)
                .toList();
        sharedNoteIds.addAll(friendSharedNoteIds);
        // 合并所有笔记ID
        List<Long> allNoteIds = new ArrayList<>();
        allNoteIds.addAll(myNoteIds);
        allNoteIds.addAll(sharedNoteIds);
        allNoteIds = allNoteIds.stream().distinct().toList();
        log.info("合并后的笔记总数: {}, 自己的: {}, 共享的: {}", 
                allNoteIds.size(), myNoteIds.size(), sharedNoteIds.size());
        if (allNoteIds.isEmpty()) {
            log.info("没有可查询的笔记，返回空页面");
            return new Page<>();
        }
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getDeleted, NoteConstant.NOT_DELETED);
        queryWrapper.in(Note::getId, allNoteIds);
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Note::getTitle, keyword);
        }
        //关联表查询根据tag去找对应的笔记
        if (StringUtils.isNotBlank(tag)) {
            LambdaQueryWrapper<NoteTag> noteTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            noteTagLambdaQueryWrapper.eq(NoteTag::getTagName, tag);
            List<NoteTag> noteTags = noteTagMapper.selectList(noteTagLambdaQueryWrapper);
            List<Long> noteIdsByTag = noteTags.stream().map(NoteTag::getNoteId).toList();
            if (!noteIdsByTag.isEmpty()) {
                queryWrapper.in(Note::getId, noteIdsByTag);
            } else {
                return new Page<>();
            }
        }
        //处理文件夹归属问题，我的笔记按文件夹展示，而他人的笔记在未分类中，他人的文件夹对我来说没有意义,要么笔记在文件夹中要么就是别人的
        if (folderId != null) {
            if (folderId == 0) {
                queryWrapper.and(w -> w.isNull(Note::getFolderId).or().notIn(Note::getId, myNoteIds));
            } else {
                queryWrapper.and(w -> w.eq(Note::getFolderId, folderId).or().notIn(Note::getId, myNoteIds));
            }
        }
        queryWrapper.orderByDesc(Note::getUpdateTime);
        Page<Note> notePage = noteMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        Page<NoteVO> voPage = (Page<NoteVO>) notePage.convert(this::toVO);
        return voPage;
    }
    @Override
    public NoteVO getById(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if(note == null || note.getDeleted() == 1){
            throw new RuntimeException("笔记不存在");
        }
        boolean isOwner = note.getUserId().equals(userId);
        if (!isOwner) {
            LambdaQueryWrapper<NotePermission> permWrapper = new LambdaQueryWrapper<>();
            permWrapper.eq(NotePermission::getNoteId, id);
            
            // 使用 list() 避免 TooManyResultsException
            List<NotePermission> permissionList = notePermissionMapper.selectList(permWrapper);
            NotePermission permission = null;
            if (permissionList != null && !permissionList.isEmpty()) {
                // 取最新的一条（按ID降序）
                permission = permissionList.stream()
                        .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                        .findFirst()
                        .orElse(null);
            }
            
            if (permission == null) {
                throw new RuntimeException("无权限访问该笔记");
            }
            Integer permType = permission.getPermissionType();
            if (permType.equals(NotePermissionConstant.TYPE_PUBLIC)) {
            }
            else if (permType.equals(NotePermissionConstant.TYPE_FRIEND_READ) 
                    || permType.equals(NotePermissionConstant.TYPE_FRIEND_EDIT)) {
                LambdaQueryWrapper<NoteShareUser> shareWrapper = new LambdaQueryWrapper<>();
                shareWrapper.eq(NoteShareUser::getNoteId, id)
                        .eq(NoteShareUser::getUserId, userId);
                long count = noteShareUserMapper.selectCount(shareWrapper);
                if (count == 0) {
                    throw new RuntimeException("无权限访问该笔记");
                }
            }
            else {
                throw new RuntimeException("无权限访问该笔记");
            }
        }
        // 记录浏览历史：清理旧记录，只保留最新的一条
        LambdaQueryWrapper<NoteHistory> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(NoteHistory::getUserId, userId)
                     .eq(NoteHistory::getNoteId, note.getId());
        
        // 先查询是否存在记录
        long count = noteHistoryMapper.selectCount(historyWrapper);
        
        if (count > 0) {
            // 删除所有旧记录
            noteHistoryMapper.delete(historyWrapper);
            log.info("清理旧浏览历史: userId={}, noteId={}, 删除数量={}", userId, note.getId(), count);
        }
        
        // 插入新的浏览记录
        NoteHistory noteHistory = new NoteHistory();
        noteHistory.setUserId(userId);
        noteHistory.setNoteId(note.getId());
        noteHistory.setViewTime(LocalDateTime.now());
        noteHistoryMapper.insert(noteHistory);
        log.info("创建浏览历史: userId={}, noteId={}", userId, note.getId());
        return toVO(note);
    }
    @Override
    public void updateById(NoteUpdateDTO noteUpdateDTO, Long id, Long userId) {
        Note oldNote = noteMapper.selectById(id);
        if(oldNote==null||oldNote.getDeleted()==1|| !oldNote.getUserId().equals(userId)){
            throw new RuntimeException("不存在或无权限");
        }
        Note note = new Note();
        note.setId(id);
        note.setUserId(userId);
        note.setTitle(noteUpdateDTO.getTitle());
        note.setContent(noteUpdateDTO.getContent());
        // 如果没有指定文件夹，自动分配到默认文件夹
        if (noteUpdateDTO.getFolderId() == null) {
            Long defaultFolderId = getDefaultFolderId(userId);
            note.setFolderId(defaultFolderId);
            log.info("更新笔记时未指定文件夹，自动分配到默认文件夹: {}", defaultFolderId);
        } else {
            note.setFolderId(noteUpdateDTO.getFolderId());
        }
        note.setUpdateTime(LocalDateTime.now());
        log.info("更新笔记: ID={}, folderId={}", id, note.getFolderId());
        int rows = noteMapper.updateById(note);
        log.info("更新结果: 影响行数={}", rows);
        Note updatedNote = noteMapper.selectById(id);
        log.info("更新后验证: ID={}, folderId={}", id, updatedNote != null ? updatedNote.getFolderId() : "null");
        noteTagMapper.delete(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, id));//还要改保留不了原来标签
        if (noteUpdateDTO.getTags() != null && !noteUpdateDTO.getTags().isEmpty()) {
            for (String tagName : noteUpdateDTO.getTags()) {
                NoteTag tag = new NoteTag();
                tag.setNoteId(id);
                tag.setTagName(tagName);
                noteTagMapper.insert(tag);
            }
        }
    }
    @Override
    public void deleteById(Long userId, Long id) {
        Note note = noteMapper.selectById(id);
        if(note==null||note.getDeleted()==1|| !note.getUserId().equals(userId)){
            throw new RuntimeException("不存在或无权限");
        }
        recycleBinService.addToRecycleBin("note", id, userId);
        noteMapper.deleteById(id);
        log.info("笔记已移至回收站：ID={}, 用户ID={}", id, userId);
    }
    @Override
    public Page<NoteVO> getHistory(Integer pageNum, Integer pageSize, Long userId) {
        log.info("获取笔记历史记录: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        // 查询用户的历史记录，按查看时间降序
        LambdaQueryWrapper<NoteHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteHistory::getUserId, userId);
        queryWrapper.orderByDesc(NoteHistory::getViewTime);
        Page<NoteHistory> historyPage = noteHistoryMapper.selectPage(
                new Page<>(pageNum, pageSize), queryWrapper);
        
        if (historyPage.getRecords().isEmpty()) {
            log.info("没有历史记录");
            return new Page<>(pageNum, pageSize, 0);
        }
        
        // 去重：同一篇笔记只保留最新的记录（因为已经按时间降序，所以第一个就是最新的）
        // 同时构建 noteId -> viewTime 的映射
        java.util.Map<Long, LocalDateTime> noteViewTimeMap = new java.util.HashMap<>();
        List<Long> uniqueNoteIds = historyPage.getRecords().stream()
                .filter(history -> {
                    // 只保留第一次出现的noteId（即最新的记录）
                    if (!noteViewTimeMap.containsKey(history.getNoteId())) {
                        noteViewTimeMap.put(history.getNoteId(), history.getViewTime());
                        return true;
                    }
                    return false;
                })
                .map(NoteHistory::getNoteId)
                .toList();
        
        log.info("去重后的笔记数量: {}", uniqueNoteIds.size());
        
        if (uniqueNoteIds.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }
        
        // 批量查询笔记详情
        List<Note> noteList = noteMapper.selectBatchIds(uniqueNoteIds);
        List<NoteVO> voList = noteList.stream()
                .map(this::toVO)
                .peek(vo -> {
                    // 设置浏览时间
                    if (noteViewTimeMap.containsKey(vo.getId())) {
                        vo.setViewTime(noteViewTimeMap.get(vo.getId()));
                    }
                })
                .toList();
        
        Page<NoteVO> resultPage = new Page<>(pageNum, pageSize, historyPage.getTotal());
        resultPage.setRecords(voList);
        log.info("返回历史记录数量: {}", voList.size());
        return resultPage;
    }
    
    /**
     * 获取用户的默认文件夹ID（第一个创建的文件夹）
     */
    private Long getDefaultFolderId(Long userId) {
        LambdaQueryWrapper<com.smartnote.pojo.NoteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.smartnote.pojo.NoteFolder::getUserId, userId)
               .orderByAsc(com.smartnote.pojo.NoteFolder::getCreateTime)
               .orderByAsc(com.smartnote.pojo.NoteFolder::getId); // 添加ID作为第二排序条件，确保唯一性
        
        // 使用 list() 避免 TooManyResultsException
        List<com.smartnote.pojo.NoteFolder> folderList = noteFolderService.list(wrapper);
        if (folderList != null && !folderList.isEmpty()) {
            return folderList.get(0).getId();
        }
        return null;
    }
}

