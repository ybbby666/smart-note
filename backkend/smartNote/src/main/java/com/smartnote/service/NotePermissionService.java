package com.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.pojo.NotePermission;
import com.smartnote.vo.NotePermissionVO;
import com.smartnote.dto.NotePermissionDTO;

public interface NotePermissionService extends IService<NotePermission> {
    void updatePermission(NotePermissionDTO notePermissionDTO, Long userId);

    NotePermissionVO getPermission(Long noteId, Long userId);

    boolean canRead(Long noteId, Long userId);

    // ====================== 4. 校验可编辑权限 ======================
    boolean canEdit(Long noteId, Long userId);
}
