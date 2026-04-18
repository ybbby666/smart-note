package com.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.dto.NoteAnnotationCreateDTO;
import com.smartnote.dto.NoteAnnotationUpdateDTO;
import com.smartnote.pojo.NoteAnnotation;
import com.smartnote.vo.NoteAnnotationVO;

import java.util.List;

public interface NoteAnnotationService extends IService<NoteAnnotation> {
    
    /**
     * 创建批注
     */
    Long createAnnotation(Long noteId, NoteAnnotationCreateDTO dto, Long userId);
    
    /**
     * 获取笔记的所有批注
     */
    List<NoteAnnotationVO> getAnnotationsByNoteId(Long noteId, Long userId);
    
    /**
     * 更新批注
     */
    void updateAnnotation(Long annotationId, NoteAnnotationUpdateDTO dto, Long userId);
    
    /**
     * 删除批注
     */
    void deleteAnnotation(Long annotationId, Long userId);
}
