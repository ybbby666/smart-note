package com.smartnote.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记批注实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("note_annotation")
public class NoteAnnotation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private String content;
    private String position;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
