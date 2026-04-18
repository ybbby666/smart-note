package com.smartnote.pojo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("note_id")
    private Long noteId;
    @TableField("tag_name")
    private String tagName;
}
