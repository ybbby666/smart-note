package com.smartnote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.pojo.Note;
import com.smartnote.vo.NoteVO;
import com.smartnote.dto.NoteAddDTO;
import com.smartnote.dto.NoteUpdateDTO;

public interface NoteService extends IService<Note> {
    Long addNote(NoteAddDTO noteAddDTO,Long userId);

    Object page(Integer pageNum, Integer pageSize, String keyword, String tag, Long folderId, Long userId);

    NoteVO getById(Long id, Long userId);

    void updateById(NoteUpdateDTO noteUpdateDTO, Long id, Long userId);

    void deleteById(Long userId, Long id);


    Page<NoteVO> getHistory(Integer pageNum, Integer pageSize, Long userId);
}
