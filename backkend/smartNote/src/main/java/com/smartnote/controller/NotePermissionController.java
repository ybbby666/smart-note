package com.smartnote.controller;

import com.smartnote.pojo.Result;
import com.smartnote.service.NotePermissionService;
import com.smartnote.vo.NotePermissionVO;
import com.smartnote.dto.NotePermissionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notes")
public class NotePermissionController {
    @Autowired
    private NotePermissionService notePermissionService;
    @PutMapping("{noteId}/permission")
    public Result updatePermission(@PathVariable Long noteId,
                                   @RequestBody NotePermissionDTO notePermissionDTO,
                                   @RequestAttribute Long userId)
    {
        notePermissionDTO.setNoteId(noteId);
        notePermissionService.updatePermission(notePermissionDTO,userId);
        log.info("权限设置成功");
        return Result.success();
    }
    @GetMapping("/{noteId}/permission")
    public Result getPermission(@PathVariable Long noteId,
                                @RequestAttribute Long userId) {
        NotePermissionVO vo = notePermissionService.getPermission(noteId, userId);
        log.info("获取权限");
        return Result.success(vo);
    }

}
