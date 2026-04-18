package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.constants.NotePermissionConstant;
import com.smartnote.mapper.*;
import com.smartnote.pojo.*;
import com.smartnote.service.NotePermissionService;
import com.smartnote.vo.NotePermissionVO;
import com.smartnote.vo.UserSearchVO;
import com.smartnote.dto.NotePermissionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class NotePermissionServiceImpl extends ServiceImpl<NotePermissionMapper, NotePermission> implements NotePermissionService {
   @Autowired
   NotePermissionMapper notePermissionMapper;
   @Autowired
   NoteMapper noteMapper;
   @Autowired
   FriendMapper friendMapper;
   @Autowired
   NoteShareUserMapper noteShareUserMapper;
   @Autowired
   UserMapper userMapper;
    private UserSearchVO toVO(User user) {
        if (user == null) return null;
        UserSearchVO vo = new UserSearchVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        return vo;
    }
    @Override
    public void updatePermission(NotePermissionDTO notePermissionDTO, Long userId) {
        log.info("开始更新笔记权限: noteId={}, userId={}, permissionType={}, shareUserIdList={}", 
                notePermissionDTO.getNoteId(), userId, notePermissionDTO.getPermissionType(), notePermissionDTO.getShareUserIdList());
        Note note = noteMapper.selectById(notePermissionDTO.getNoteId());
        if (note == null || !note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该笔记");
        }
        Integer type = notePermissionDTO.getPermissionType();
        List<Long> shareUserIdList = notePermissionDTO.getShareUserIdList();
        if (shareUserIdList != null && !shareUserIdList.isEmpty()) {
            log.info("验证{}个共享用户的好友关系", shareUserIdList.size());
            for (Long shareUserId : shareUserIdList) {
                // 不能将自己添加为共享用户
                if (shareUserId.equals(userId)) {
                    log.warn("用户{}尝试将自己添加为共享用户", userId);
                    throw new RuntimeException("不能将自己添加为共享用户");
                }
                // 双向检查好友关系：A是B的好友 或 B是A的好友
                boolean isFriend = friendMapper.exists(
                        new LambdaQueryWrapper<Friend>()
                                .eq(Friend::getUserId, userId)
                                .eq(Friend::getFriendUserId, shareUserId)
                ) || friendMapper.exists(
                        new LambdaQueryWrapper<Friend>()
                                .eq(Friend::getUserId, shareUserId)
                                .eq(Friend::getFriendUserId, userId)
                );
                
                if (!isFriend) {
                    log.warn("用户{}与用户{}不是好友关系", userId, shareUserId);
                    throw new RuntimeException("只能分享给好友");
                }
                log.info("用户{}与用户{}是好友关系，验证通过", userId, shareUserId);
            }
        }
        LambdaQueryWrapper<NotePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotePermission::getNoteId, notePermissionDTO.getNoteId());
        
        // 检查是否存在多条记录，如果有则清理
        long count = notePermissionMapper.selectCount(wrapper);
        if (count > 1) {
            log.warn("发现{}条重复的权限记录，清理旧数据: noteId={}", count, notePermissionDTO.getNoteId());
            notePermissionMapper.delete(wrapper);
        }
        
        NotePermission existPermission = notePermissionMapper.selectOne(wrapper);
        if (existPermission == null) {
            log.info("创建新的权限记录");
            NotePermission permission = new NotePermission();
            permission.setNoteId(notePermissionDTO.getNoteId());
            permission.setPermissionType(type);
            permission.setCreateUserId(userId);
            notePermissionMapper.insert(permission);
        } else {
            log.info("更新现有权限记录，原类型: {}, 新类型: {}", existPermission.getPermissionType(), type);
            NotePermission update = new NotePermission();
            update.setId(existPermission.getId());
            update.setPermissionType(type);
            notePermissionMapper.updateById(update);
        }
        // 先删除旧的共享用户记录
        LambdaQueryWrapper<NoteShareUser> shareWrapper = new LambdaQueryWrapper<>();
        shareWrapper.eq(NoteShareUser::getNoteId, notePermissionDTO.getNoteId());
        long deletedCount = noteShareUserMapper.delete(shareWrapper);
        log.info("删除了{}条旧的共享用户记录", deletedCount);
        // 插入新的共享用户记录
        if (shareUserIdList != null && !shareUserIdList.isEmpty()) {
            log.info("准备插入{}个共享用户记录，权限类型: {}", shareUserIdList.size(), type);
            if (type.equals(NotePermissionConstant.TYPE_FRIEND_READ)) {
                for (Long shareUserId : shareUserIdList) {
                    NoteShareUser shareUser = new NoteShareUser();
                    shareUser.setNoteId(notePermissionDTO.getNoteId());
                    shareUser.setUserId(shareUserId);
                    shareUser.setPermissionType(NotePermissionConstant.SHARE_READ);
                    noteShareUserMapper.insert(shareUser);
                    log.info("插入共享用户记录: noteId={}, userId={}, permissionType=READ", 
                            notePermissionDTO.getNoteId(), shareUserId);
                }
            } else if (type.equals(NotePermissionConstant.TYPE_FRIEND_EDIT)) {
                for (Long shareUserId : shareUserIdList) {
                    NoteShareUser shareUser = new NoteShareUser();
                    shareUser.setNoteId(notePermissionDTO.getNoteId());
                    shareUser.setUserId(shareUserId);
                    shareUser.setPermissionType(NotePermissionConstant.SHARE_EDIT);
                    noteShareUserMapper.insert(shareUser);
                    log.info("插入共享用户记录: noteId={}, userId={}, permissionType=EDIT", 
                            notePermissionDTO.getNoteId(), shareUserId);
                }
            } else {
                log.warn("权限类型不是好友共享类型，不插入共享用户记录。类型: {}", type);
            }
        } else {
            log.info("共享用户列表为空，不插入共享用户记录");
        }
        
        log.info("权限更新完成");
    }
    @Override
    public NotePermissionVO getPermission(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限查看该笔记权限");
        }
        LambdaQueryWrapper<NotePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotePermission::getNoteId, noteId);
        
        // 使用 list() 避免 TooManyResultsException
        List<NotePermission> permissionList = notePermissionMapper.selectList(wrapper);
        NotePermission permission = null;
        if (permissionList != null && !permissionList.isEmpty()) {
            // 取最新的一条（按ID降序）
            permission = permissionList.stream()
                    .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                    .findFirst()
                    .orElse(null);
        }
        if (permission == null) {
            NotePermissionVO vo = new NotePermissionVO();
            vo.setNoteId(noteId);
            vo.setPermissionType(NotePermissionConstant.TYPE_PRIVATE);
            vo.setShareUserIdList(Collections.emptyList());
            vo.setShareUserList(Collections.emptyList());
            return vo;
        }
        LambdaQueryWrapper<NoteShareUser> shareWrapper = new LambdaQueryWrapper<>();
        shareWrapper.eq(NoteShareUser::getNoteId, noteId);
        List<NoteShareUser> shareUserList = noteShareUserMapper.selectList(shareWrapper);
        List<Long> shareUserIdList = shareUserList.stream()
                .map(NoteShareUser::getUserId)
                .toList();

        List<User> userList = Collections.emptyList();
        if (shareUserIdList != null && !shareUserIdList.isEmpty()) {
            userList = userMapper.selectBatchIds(shareUserIdList);
        }
        List<UserSearchVO> userVOList = userList.stream()
                .map(this::toVO)
                .filter(Objects::nonNull)
                .toList();
        NotePermissionVO vo = new NotePermissionVO();
        vo.setNoteId(noteId);
        vo.setPermissionType(permission.getPermissionType());
        vo.setShareUserIdList(shareUserIdList);
        vo.setShareUserList(userVOList);
        return vo;
        }
    @Override
    public boolean canRead(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) return false;
        // 创建者永远可读
        if (note.getUserId().equals(userId)) return true;
        LambdaQueryWrapper<NotePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotePermission::getNoteId, noteId);
        
        // 使用 list() 避免 TooManyResultsException
        List<NotePermission> permissionList = notePermissionMapper.selectList(wrapper);
        NotePermission permission = null;
        if (permissionList != null && !permissionList.isEmpty()) {
            // 取最新的一条（按ID降序）
            permission = permissionList.stream()
                    .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                    .findFirst()
                    .orElse(null);
        }
        if (permission == null) return false;
        Integer type = permission.getPermissionType();
        if (type.equals(NotePermissionConstant.TYPE_PUBLIC)) return true;
        if (type.equals(NotePermissionConstant.TYPE_FRIEND_READ)
                || type.equals(NotePermissionConstant.TYPE_FRIEND_EDIT)) {
            LambdaQueryWrapper<NoteShareUser> shareWrapper = new LambdaQueryWrapper<>();
            shareWrapper.eq(NoteShareUser::getNoteId, noteId)
                    .eq(NoteShareUser::getUserId, userId);
            return noteShareUserMapper.selectCount(shareWrapper) > 0;
        }
        return false;
    }
    @Override
    public boolean canEdit(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) return false;
        if (note.getUserId().equals(userId)) return true;
        LambdaQueryWrapper<NotePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotePermission::getNoteId, noteId);
        
        // 使用 list() 避免 TooManyResultsException
        List<NotePermission> permissionList = notePermissionMapper.selectList(wrapper);
        NotePermission permission = null;
        if (permissionList != null && !permissionList.isEmpty()) {
            // 取最新的一条（按ID降序）
            permission = permissionList.stream()
                    .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                    .findFirst()
                    .orElse(null);
        }
        if (permission == null) return false;
        Integer type = permission.getPermissionType();
        if (type.equals(NotePermissionConstant.TYPE_FRIEND_EDIT)) {
            LambdaQueryWrapper<NoteShareUser> shareWrapper = new LambdaQueryWrapper<>();
            shareWrapper.eq(NoteShareUser::getNoteId, noteId)
                    .eq(NoteShareUser::getUserId, userId)
                    .eq(NoteShareUser::getPermissionType, NotePermissionConstant.SHARE_EDIT);
            return noteShareUserMapper.selectCount(shareWrapper) > 0;
        }
        return false;
    }
    }



