package com.smartnote.controller;

import com.smartnote.dto.NoteAnnotationCreateDTO;
import com.smartnote.dto.NoteAnnotationUpdateDTO;
import com.smartnote.pojo.Result;
import com.smartnote.service.NoteAnnotationService;
import com.smartnote.vo.NoteAnnotationVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notes")
public class NoteAnnotationController {

    @Autowired
    private NoteAnnotationService noteAnnotationService;

    /**
     * 创建批注
     */
    @PostMapping("/{noteId}/annotations")
    public Result createAnnotation(@PathVariable Long noteId,
                                   @Valid @RequestBody NoteAnnotationCreateDTO dto,
                                   @RequestAttribute Long userId) {
        Long annotationId = noteAnnotationService.createAnnotation(noteId, dto, userId);
        log.info("用户{}为笔记{}创建批注{}", userId, noteId, annotationId);
        return Result.success(annotationId);
    }

    /**
     * 获取笔记的所有批注
     */
    @GetMapping("/{noteId}/annotations")
    public Result getAnnotations(@PathVariable Long noteId,
                                 @RequestAttribute(required = false) Long userId) {
        List<NoteAnnotationVO> annotations = noteAnnotationService.getAnnotationsByNoteId(noteId, userId);
        log.info("获取笔记{}的批注列表，共{}条", noteId, annotations.size());
        return Result.success(annotations);
    }

    /**
     * 更新批注
     */
    @PutMapping("/annotations/{annotationId}")
    public Result updateAnnotation(@PathVariable Long annotationId,
                                   @Valid @RequestBody NoteAnnotationUpdateDTO dto,
                                   @RequestAttribute Long userId) {
        noteAnnotationService.updateAnnotation(annotationId, dto, userId);
        log.info("用户{}更新批注{}", userId, annotationId);
        return Result.success();
    }

    /**
     * 删除批注
     */
    @DeleteMapping("/annotations/{annotationId}")
    public Result deleteAnnotation(@PathVariable Long annotationId,
                                   @RequestAttribute Long userId) {
        noteAnnotationService.deleteAnnotation(annotationId, userId);
        log.info("用户{}删除批注{}", userId, annotationId);
        return Result.success();
    }
}
