package com.smartnote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendVO {
    private Long friendId;
    private String username;
    private String email;
    private String phone;
    private String groupName;
    private LocalDateTime createTime;
    private String nickname;
    private String avatar;
    private String motto;
    private List<FriendVO> friendList;
    private Long total;
    private Boolean isFriend; // 是否已是好友
}
