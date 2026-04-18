package com.smartnote.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteVO {
    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long folderId; // 文件夹ID
    private Long creatorId; // 创建者ID
    private String creatorName; // 创建者名称
    private LocalDateTime viewTime; // 浏览时间（仅在历史记录中使用）
}