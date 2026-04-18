package com.smartnote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteAiResultDTO {
    private String summary;       // 摘要
    private List<String> keyPoints; // 要点
    private List<String> tags;
}
