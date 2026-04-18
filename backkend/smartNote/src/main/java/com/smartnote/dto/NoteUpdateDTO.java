package com.smartnote.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteUpdateDTO {
    private String title;
    private String content;
    private List<String> tags;
    private Long folderId; // 文件夹ID，null时自动分配到默认文件夹
}
