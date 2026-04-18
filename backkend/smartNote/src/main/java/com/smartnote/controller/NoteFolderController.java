package com.smartnote.controller;

import com.smartnote.dto.NoteFolderCreateDTO;
import com.smartnote.dto.NoteFolderRenameDTO;
import com.smartnote.pojo.Result;
import com.smartnote.service.NoteFolderService;
import com.smartnote.vo.NoteFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/folders")
public class NoteFolderController {

    @Autowired
    private NoteFolderService noteFolderService;

    /**
     * 创建文件夹
     */
    @PostMapping
    public Result createFolder(@Validated @RequestBody NoteFolderCreateDTO dto,
                               @RequestAttribute Long userId) {
        Long folderId = noteFolderService.createFolder(dto, userId);
        log.info("用户{}创建文件夹：{}", userId, dto.getName());
        return Result.success(folderId);
    }

    /**
     * 重命名文件夹
     */
    @PutMapping("/{folderId}")
    public Result renameFolder(@PathVariable Long folderId,
                               @Validated @RequestBody NoteFolderRenameDTO dto,
                               @RequestAttribute Long userId) {
        noteFolderService.renameFolder(folderId, dto, userId);
        log.info("用户{}重命名文件夹{}为：{}", userId, folderId, dto.getName());
        return Result.success();
    }

    /**
     * 删除文件夹
     */
    @DeleteMapping("/{folderId}")
    public Result deleteFolder(@PathVariable Long folderId,
                               @RequestAttribute Long userId) {
        noteFolderService.deleteFolder(folderId, userId);
        log.info("用户{}删除文件夹：{}", userId, folderId);
        return Result.success();
    }

    /**
     * 获取文件夹列表
     */
    @GetMapping("/tree")
    public Result getFolderTree(@RequestAttribute Long userId) {
        List<NoteFolderVO> tree = noteFolderService.getFolderTree(userId);
        log.info("用户{}获取文件夹列表", userId);
        return Result.success(tree);
    }
}
