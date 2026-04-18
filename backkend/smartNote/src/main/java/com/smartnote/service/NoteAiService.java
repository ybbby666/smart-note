package com.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.pojo.NoteAiAnalysis;
import com.smartnote.vo.NoteAiResultVO;

public interface NoteAiService extends IService<NoteAiAnalysis> {
    NoteAiResultVO analyzeNote(Long noteId, Long userId);

    NoteAiResultVO getAnalysisResult(Long noteId, Long userId);
}
