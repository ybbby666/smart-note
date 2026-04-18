package com.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建文件夹DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteFolderCreateDTO {
    
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 50, message = "文件夹名称最多50个字符")
    private String name;
    
    /**
     * 父文件夹ID，仅支持null（根目录）
     */
    private Long parentId;
}
