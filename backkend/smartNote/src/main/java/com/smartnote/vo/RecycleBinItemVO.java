package com.smartnote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 回收站项VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecycleBinItemVO {
    
    private Long id;
    private String resourceType; // note 或 folder
    private Long resourceId;
    private String resourceName; // 笔记标题或文件夹名称
    private LocalDateTime deleteTime;
    private LocalDateTime expireTime;
    private Long daysLeft; // 剩余时间（分钟）
}
