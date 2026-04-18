package com.smartnote.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotePermissionDTO {
    private Long noteId;                  // 笔记ID
    private Integer permissionType;       // 权限类型
    private List<Long> shareUserIdList;   // 共享的用户ID列表（好友可读/可编辑时使用）
}
