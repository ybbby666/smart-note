package com.smartnote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteAiResultVO {
    private Long noteId;
    private String summary;
    private List<String> keyPoints;
    private List<String> tags;
    private LocalDateTime updateTime;
}
