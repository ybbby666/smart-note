package com.smartnote.vo;

import lombok.Data;

import java.util.List;

@Data
public class NotePermissionVO {
    private Long noteId;
    private Integer permissionType;
    private List<Long> shareUserIdList;  // 已共享的用户ID列表
    private List<UserSearchVO> shareUserList; // 已共享的用户信息（复用UserSearchVO）
}