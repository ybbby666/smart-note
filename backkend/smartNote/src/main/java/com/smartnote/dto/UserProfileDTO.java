package com.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDTO {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称最多20个字符")
    private String nickname;
    
    @Size(max = 100, message = "座右铭最多100个字符")
    private String motto;
    
    @Size(max = 500, message = "头像URL过长")
    private String avatar;
}
