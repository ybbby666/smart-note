package com.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重命名文件夹DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteFolderRenameDTO {
    
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 50, message = "文件夹名称最多50个字符")
    private String name;
}
