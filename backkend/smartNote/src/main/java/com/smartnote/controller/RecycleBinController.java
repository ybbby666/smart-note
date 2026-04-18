package com.smartnote.controller;

import com.smartnote.pojo.Result;
import com.smartnote.service.RecycleBinService;
import com.smartnote.vo.RecycleBinItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recycle-bin")
public class RecycleBinController {
    @Autowired
    private RecycleBinService recycleBinService;
    @GetMapping
    public Result getRecycleBinList(@RequestAttribute Long userId) {
        List<RecycleBinItemVO> list = recycleBinService.getRecycleBinList(userId);
        log.info("用户{}获取回收站列表", userId);
        return Result.success(list);
    }
    @PutMapping("/{recycleBinId}/restore")
    public Result restoreFromRecycleBin(@PathVariable Long recycleBinId,
                                        @RequestAttribute Long userId) {
        recycleBinService.restoreFromRecycleBin(recycleBinId, userId);
        log.info("用户{}恢复资源：{}", userId, recycleBinId);
        return Result.success();
    }
    @DeleteMapping("/{recycleBinId}")
    public Result permanentDelete(@PathVariable Long recycleBinId,
                                  @RequestAttribute Long userId) {
        recycleBinService.permanentDelete(recycleBinId, userId);
        log.info("用户{}彻底删除：{}", userId, recycleBinId);
        return Result.success();
    }
    @DeleteMapping("/clear")
    public Result clearRecycleBin(@RequestAttribute Long userId) {
        recycleBinService.clearRecycleBin(userId);
        log.info("用户{}清空回收站", userId);
        return Result.success();
    }
}
