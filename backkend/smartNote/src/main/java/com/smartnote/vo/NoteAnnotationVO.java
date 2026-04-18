package com.smartnote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记批注VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteAnnotationVO {
    
    /**
     * 批注ID
     */
    private Long id;
    
    /**
     * 笔记ID
     */
    private Long noteId;
    
    /**
     * 批注内容
     */
    private String content;
    
    /**
     * 批注位置
     */
    private String position;
    
    /**
     * 批注人ID
     */
    private Long userId;
    
    /**
     * 批注人姓名
     */
    private String username;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
