package com.smartnote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件夹VO（仅一级，无嵌套）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteFolderVO {
    
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private LocalDateTime createTime;
    
    /**
     * 子文件夹列表（保留字段，但始终为空）
     */
    private List<NoteFolderVO> children;
    
    /**
     * 文件夹下的笔记数量
     */
    private Integer noteCount;
}
