package com.smartnote.pojo;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("note_permission")
public class NotePermission {
    private Long id;
    private Long noteId;
    private Integer permissionType;
    private Long createUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
