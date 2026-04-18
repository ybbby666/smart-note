package com.smartnote.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note_share_user")
public class NoteShareUser {
    private Long id;
    private Long noteId;
    private Long userId;
    private Integer permissionType;
    private LocalDateTime createTime;
}
