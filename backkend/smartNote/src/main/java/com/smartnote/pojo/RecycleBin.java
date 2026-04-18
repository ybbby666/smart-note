package com.smartnote.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 回收站实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("recycle_bin")
public class RecycleBin {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资源类型：note-笔记, folder-文件夹
     */
    private String resourceType;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 资源名称（笔记标题或文件夹名称）
     */
    private String resourceName;
    
    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;
    
    /**
     * 过期时间（5分钟后自动清理，用于答辩演示）
     */
    private LocalDateTime expireTime;
}
