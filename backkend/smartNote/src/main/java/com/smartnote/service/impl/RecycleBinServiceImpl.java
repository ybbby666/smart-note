package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.exception.BusinessException;
import com.smartnote.mapper.NoteFolderMapper;
import com.smartnote.mapper.NoteMapper;
import com.smartnote.mapper.RecycleBinMapper;
import com.smartnote.pojo.Note;
import com.smartnote.pojo.NoteFolder;
import com.smartnote.pojo.RecycleBin;
import com.smartnote.service.RecycleBinService;
import com.smartnote.vo.RecycleBinItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, RecycleBin> implements RecycleBinService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteFolderMapper noteFolderMapper;
    @Autowired
    private RecycleBinMapper recycleBinMapper;  // 注入RecycleBinMapper用于物理删除
    private static final int EXPIRE_MINUTES = 5; // 5分钟后自动清理
    @Override
    @Transactional
    public void addToRecycleBin(String resourceType, Long resourceId, Long userId) {
        log.info("添加到回收站：类型={}, ID={}, 用户ID={}", resourceType, resourceId, userId);
        RecycleBin recycleBin = new RecycleBin();
        recycleBin.setResourceType(resourceType);
        recycleBin.setResourceId(resourceId);
        recycleBin.setUserId(userId);
        // 获取资源名称
        String resourceName = "";
        if ("note".equals(resourceType)) {
            Note note = noteMapper.selectById(resourceId);
            resourceName = note != null ? note.getTitle() : "已删除的笔记";
        } else if ("folder".equals(resourceType)) {
            NoteFolder folder = noteFolderMapper.selectById(resourceId);
            resourceName = folder != null ? folder.getName() : "已删除的文件夹";
        }
        recycleBin.setResourceName(resourceName);
        recycleBin.setDeleteTime(LocalDateTime.now());
        recycleBin.setExpireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        save(recycleBin);
        log.info("回收站记录创建成功：ID={}, 资源名称={}", recycleBin.getId(), resourceName);
    }
    @Override
    public List<RecycleBinItemVO> getRecycleBinList(Long userId) {
        log.info("获取回收站列表：用户ID={}", userId);
        cleanExpiredItemsForUser(userId);
        LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecycleBin::getUserId, userId)
               .orderByDesc(RecycleBin::getDeleteTime);
        List<RecycleBin> items = list(wrapper);
        return items.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void restoreFromRecycleBin(Long recycleBinId, Long userId) {
        log.info("恢复资源：回收站ID={}, 用户ID={}", recycleBinId, userId);
        RecycleBin recycleBin = getById(recycleBinId);
        if (recycleBin == null || !recycleBin.getUserId().equals(userId)) {
            throw new BusinessException("回收站记录不存在");
        }
        String resourceType = recycleBin.getResourceType();
        Long resourceId = recycleBin.getResourceId();
        if ("note".equals(resourceType)) {
            int rows = noteMapper.restoreNote(resourceId);
            log.info("笔记恢复结果：ID={}, 影响行数={}", resourceId, rows);
            if (rows == 0) {
                log.warn("笔记恢复失败，可能笔记不存在：ID={}", resourceId);
            }
        } else if ("folder".equals(resourceType)) {
            int rows = noteFolderMapper.restoreFolder(resourceId);
            log.info("文件夹恢复结果：ID={}, 影响行数={}", resourceId, rows);
            if (rows == 0) {
                log.warn("文件夹恢复失败，可能文件夹不存在：ID={}", resourceId);
            }
        }
        removeById(recycleBinId);
        log.info("资源恢复完成");
    }
    @Override
    @Transactional
    public void permanentDelete(Long recycleBinId, Long userId) {
        log.info("彻底删除：回收站ID={}, 用户ID={}", recycleBinId, userId);
        RecycleBin recycleBin = getById(recycleBinId);
        if (recycleBin == null || !recycleBin.getUserId().equals(userId)) {
            throw new BusinessException("回收站记录不存在");
        }
        String resourceType = recycleBin.getResourceType();
        Long resourceId = recycleBin.getResourceId();
        if ("note".equals(resourceType)) {
            // 物理删除笔记
            int rows = recycleBinMapper.physicallyDeleteNote(resourceId);
            log.info("笔记彻底删除：ID={}, 影响行数={}", resourceId, rows);
        } else if ("folder".equals(resourceType)) {
            // 物理删除文件夹及其子文件夹
            deleteFolderPermanently(resourceId);
            log.info("文件夹彻底删除：ID={}", resourceId);
        }
        removeById(recycleBinId);
        log.info("彻底删除完成");
    }
    @Override
    @Transactional
    public void clearRecycleBin(Long userId) {
        log.info("清空回收站：用户ID={}", userId);
        LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecycleBin::getUserId, userId);
        List<RecycleBin> items = list(wrapper);
        for (RecycleBin item : items) {
            permanentDelete(item.getId(), userId);
        }
        log.info("回收站清空完成");
    }
    @Override
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    @Transactional
    public void cleanExpiredItems() {
        log.info("开始清理过期回收站项目");
        LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(RecycleBin::getExpireTime, LocalDateTime.now());
        List<RecycleBin> expiredItems = list(wrapper);
        for (RecycleBin item : expiredItems) {
            try {
                permanentDelete(item.getId(), item.getUserId());
            } catch (Exception e) {
                log.error("清理过期项目失败：ID={}", item.getId(), e);
            }
        }
        if (!expiredItems.isEmpty()) {
            log.info("清理完成，共处理{}个过期项目", expiredItems.size());
        }
    }
    private void deleteFolderPermanently(Long folderId) {
        // 先递归删除子文件夹
        LambdaQueryWrapper<NoteFolder> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(NoteFolder::getParentId, folderId);
        List<NoteFolder> children = noteFolderMapper.selectList(childWrapper);
        for (NoteFolder child : children) {
            deleteFolderPermanently(child.getId());
        }
        // 物理删除当前文件夹
        int rows = recycleBinMapper.physicallyDeleteFolder(folderId);
        log.debug("文件夹物理删除：ID={}, 影响行数={}", folderId, rows);
    }
    private void cleanExpiredItemsForUser(Long userId) {
        log.debug("开始清理用户{}的过期回收站项目", userId);
        LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecycleBin::getUserId, userId)
               .lt(RecycleBin::getExpireTime, LocalDateTime.now());
        List<RecycleBin> expiredItems = list(wrapper);
        for (RecycleBin item : expiredItems) {
            try {
                permanentDelete(item.getId(), userId);
            } catch (Exception e) {
                log.error("清理过期项目失败：ID={}", item.getId(), e);
            }
        }
        if (!expiredItems.isEmpty()) {
            log.info("为用户{}清理了{}个过期回收站项目", userId, expiredItems.size());
        }
    }
    private RecycleBinItemVO convertToVO(RecycleBin recycleBin) {
        RecycleBinItemVO vo = new RecycleBinItemVO();
        vo.setId(recycleBin.getId());
        vo.setResourceType(recycleBin.getResourceType());
        vo.setResourceId(recycleBin.getResourceId());
        vo.setResourceName(recycleBin.getResourceName());
        vo.setDeleteTime(recycleBin.getDeleteTime());
        vo.setExpireTime(recycleBin.getExpireTime());
        // 计算剩余时间
        long minutesLeft = Duration.between(LocalDateTime.now(), recycleBin.getExpireTime()).toMinutes();
        vo.setDaysLeft(Math.max(0, minutesLeft));
        
        return vo;
    }
}
