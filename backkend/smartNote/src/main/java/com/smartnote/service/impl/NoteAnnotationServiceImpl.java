package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.exception.BusinessException;
import com.smartnote.mapper.NoteAnnotationMapper;
import com.smartnote.mapper.NoteMapper;
import com.smartnote.mapper.UserMapper;
import com.smartnote.pojo.Note;
import com.smartnote.pojo.NoteAnnotation;
import com.smartnote.pojo.User;
import com.smartnote.service.NoteAnnotationService;
import com.smartnote.service.NotePermissionService;
import com.smartnote.dto.NoteAnnotationCreateDTO;
import com.smartnote.dto.NoteAnnotationUpdateDTO;
import com.smartnote.vo.NoteAnnotationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoteAnnotationServiceImpl extends ServiceImpl<NoteAnnotationMapper, NoteAnnotation> implements NoteAnnotationService {

    @Autowired
    private NoteAnnotationMapper noteAnnotationMapper;
    
    @Autowired
    private NoteMapper noteMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotePermissionService notePermissionService;

    @Override
    @Transactional
    public Long createAnnotation(Long noteId, NoteAnnotationCreateDTO dto, Long userId) {
        log.info("创建批注：笔记ID={}, 用户ID={}", noteId, userId);
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getDeleted() == 1) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId) && !notePermissionService.canEdit(noteId, userId)) {
            throw new BusinessException("无权限在此笔记上添加批注");
        }
        NoteAnnotation annotation = new NoteAnnotation();
        annotation.setNoteId(noteId);
        annotation.setContent(dto.getContent());
        annotation.setPosition(dto.getPosition());
        annotation.setUserId(userId);
        annotation.setCreateTime(LocalDateTime.now());
        annotation.setUpdateTime(LocalDateTime.now());
        save(annotation);
        log.info("批注创建成功：ID={}", annotation.getId());
        return annotation.getId();
    }
    @Override
    public List<NoteAnnotationVO> getAnnotationsByNoteId(Long noteId, Long userId) {
        log.info("获取笔记批注列表：笔记ID={}, 用户ID={}", noteId, userId);
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getDeleted() == 1) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId) && !notePermissionService.canRead(noteId, userId)) {
            throw new BusinessException("无权限查看此笔记的批注");
        }
        LambdaQueryWrapper<NoteAnnotation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteAnnotation::getNoteId, noteId)
               .orderByAsc(NoteAnnotation::getCreateTime);
        List<NoteAnnotation> annotations = list(wrapper);
        return annotations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void updateAnnotation(Long annotationId, NoteAnnotationUpdateDTO dto, Long userId) {
        log.info("更新批注：批注ID={}, 用户ID={}", annotationId, userId);
        NoteAnnotation annotation = getById(annotationId);
        if (annotation == null) {
            throw new BusinessException("批注不存在");
        }
        if (!annotation.getUserId().equals(userId)) {
            throw new BusinessException("无权限修改此批注");
        }
        annotation.setContent(dto.getContent());
        annotation.setPosition(dto.getPosition());
        annotation.setUpdateTime(LocalDateTime.now());
        updateById(annotation);
        log.info("批注更新成功：ID={}", annotationId);
    }
    @Override
    @Transactional
    public void deleteAnnotation(Long annotationId, Long userId) {
        log.info("删除批注：批注ID={}, 用户ID={}", annotationId, userId);
        NoteAnnotation annotation = getById(annotationId);
        if (annotation == null) {
            throw new BusinessException("批注不存在");
        }
        Note note = noteMapper.selectById(annotation.getNoteId());
        if (!annotation.getUserId().equals(userId) && 
            (note == null || !note.getUserId().equals(userId))) {
            throw new BusinessException("无权限删除此批注");
        }
        removeById(annotationId);
        log.info("批注删除成功：ID={}", annotationId);
    }
    private NoteAnnotationVO convertToVO(NoteAnnotation annotation) {
        NoteAnnotationVO vo = new NoteAnnotationVO();
        vo.setId(annotation.getId());
        vo.setNoteId(annotation.getNoteId());
        vo.setContent(annotation.getContent());
        vo.setPosition(annotation.getPosition());
        vo.setUserId(annotation.getUserId());
        vo.setCreateTime(annotation.getCreateTime());
        vo.setUpdateTime(annotation.getUpdateTime());
        User user = userMapper.selectById(annotation.getUserId());
        vo.setUsername(user != null ? user.getUsername() : "未知用户");
        return vo;
    }
}
