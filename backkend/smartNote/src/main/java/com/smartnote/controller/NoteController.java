package com.smartnote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartnote.pojo.Result;
import com.smartnote.service.NoteService;
import com.smartnote.vo.NoteVO;
import com.smartnote.dto.NoteAddDTO;
import com.smartnote.dto.NoteUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notes")
public class NoteController {
    @Autowired
    private NoteService noteService;
    @PostMapping
    public Result addNote(@RequestBody NoteAddDTO noteAddDTO, @RequestAttribute Long userId) {
        Long noteId = noteService.addNote(noteAddDTO, userId);
        log.info("用户新增笔记，ID={}", noteId);
        return Result.success(noteId);
    }
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10")Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String tag,
                       @RequestParam(required = false) Long folderId,
                       @RequestAttribute Long userId
    )
    {   log.info("查询笔记, folderId={}", folderId);
        return Result.success(noteService.page(pageNum,pageSize,keyword,tag,folderId,userId));
    }
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id,@RequestAttribute Long userId) {
        log.info("id为{}的用户查看id为{}的笔记",userId,id);
        NoteVO vo=noteService.getById(id,userId);
        return Result.success(vo);
    }
    
    /**
     * 更新笔记
     * PUT /notes/{id}
     */
    @PutMapping("/{id}")
    public Result updateNote(@PathVariable Long id, @RequestBody NoteUpdateDTO noteUpdateDTO, @RequestAttribute Long userId) {
        log.info("id为{}的用户修改id为{}的笔记",userId,id);
        noteService.updateById(noteUpdateDTO,id,userId);
        return Result.success();
    }
    
    /**
     * 删除笔记（移入回收站）
     * DELETE /notes/{id}
     */
    @DeleteMapping("/{id}")
    public Result deleteNote(@PathVariable Long id,@RequestAttribute Long userId) {
        log.info("id为{}的用户删除id为{}的笔记",userId,id);
        noteService.deleteById(userId,id);
        return Result.success();
    }
    
    /**
     * 获取笔记历史记录
     * GET /notes/history?pageNum=1&pageSize=10
     */
    @GetMapping("/history")
    public Result getHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestAttribute Long userId)
    {
        log.info("查看历史纪录");
        Page<NoteVO> historyPage=noteService.getHistory(pageNum, pageSize, userId);
        return Result.success(historyPage);
    }
}