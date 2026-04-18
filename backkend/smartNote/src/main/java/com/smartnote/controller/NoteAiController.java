package com.smartnote.controller;

import com.smartnote.pojo.Result;
import com.smartnote.service.NoteAiService;
import com.smartnote.vo.NoteAiResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteAiController {
    @Autowired
    private NoteAiService noteAiService;
    
    /**
     * AI分析笔记
     * POST /notes/{noteId}/ai-analyze
     */
    @PostMapping("/{noteId}/ai-analyze")
    public Result aiAnalyze(@PathVariable Long noteId, @RequestAttribute Long userId){
        NoteAiResultVO vo = noteAiService.analyzeNote(noteId, userId);
        log.info("对笔记进行ai分析");
        return Result.success(vo);
    }
    
    /**
     * 获取AI分析结果
     * GET /notes/{noteId}/ai-result
     */
    @GetMapping("/{noteId}/ai-result")
    public Result getAiResult(@PathVariable Long noteId, @RequestAttribute Long userId) {
        NoteAiResultVO vo = noteAiService.getAnalysisResult(noteId, userId);
        return Result.success(vo);
    }
}
