package com.smartnote.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记文件夹实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("note_folder")
public class NoteFolder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    // 父文件夹ID，null表示根文件夹
    private Long parentId;
    private Long userId;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
