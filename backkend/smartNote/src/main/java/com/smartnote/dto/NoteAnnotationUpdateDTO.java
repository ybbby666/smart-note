package com.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteAnnotationUpdateDTO {
    
    @NotBlank(message = "批注内容不能为空")
    @Size(max = 1000, message = "批注内容最多1000 个字符")
    private String content;
    private String position;
}
