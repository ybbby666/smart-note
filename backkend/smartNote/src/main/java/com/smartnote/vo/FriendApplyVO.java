package com.smartnote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendApplyVO {
    private Long applyId;
    private Long applyUserId;
    private String applyUsername;
    private String applyNickname;
    private String applyAvatar;
    private Integer status;
    private LocalDateTime createTime;
}
