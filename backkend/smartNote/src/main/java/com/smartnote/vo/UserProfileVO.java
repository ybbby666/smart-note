package com.smartnote.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private String motto;
}
