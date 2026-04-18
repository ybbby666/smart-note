package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.constants.FriendConstant;
import com.smartnote.mapper.FriendApplyMapper;
import com.smartnote.mapper.FriendMapper;
import com.smartnote.mapper.UserMapper;
import com.smartnote.pojo.Friend;
import com.smartnote.pojo.FriendApply;
import com.smartnote.pojo.User;
import com.smartnote.service.FriendService;
import com.smartnote.vo.FriendApplyVO;
import com.smartnote.vo.FriendVO;
import com.smartnote.dto.UserSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FriendApplyMapper friendApplyMapper;
    private FriendVO toVO(User user) {
        if (user == null) return null;
        FriendVO vo = new FriendVO();
        vo.setFriendId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setNickname(user.getNickname());
        return vo;
    }
    @Override
    public FriendVO searchFriend(UserSearchDTO userSearchDTO, Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, userSearchDTO.getKeyword())
                .or()
                .eq(User::getPhone, userSearchDTO.getKeyword());
        
        // 使用 list() 避免 TooManyResultsException
        List<User> userList = userMapper.selectList(wrapper);
        User user = null;
        if (userList != null && !userList.isEmpty()) {
            // 取第一条记录
            user = userList.get(0);
        }
        
        if (user == null) {
            return null;
        }
        if (user.getId().equals(userId)) {
            log.info("用户{}搜索了自己", userId);
            return null;
        }
        FriendVO vo = toVO(user);
        LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
        friendWrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendUserId, user.getId());
        long count = friendMapper.selectCount(friendWrapper);
        vo.setIsFriend(count > 0);
        return vo;
    }
    @Override
    public void sentRequest(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getFriendUserId, targetUserId);
        if (friendMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已有该好友");
        }
        LambdaQueryWrapper<FriendApply> wrapperApply = new LambdaQueryWrapper<>();
        wrapperApply.eq(FriendApply::getTargetUserId, targetUserId)
                .eq(FriendApply::getStatus, FriendConstant.APPLY_WAIT)
                .eq(FriendApply::getUserId, userId);
        if (friendApplyMapper.selectCount(wrapperApply) > 0) {
            throw new RuntimeException("已发送好友申请，请等待对方回复");
        }
        FriendApply friendApply = new FriendApply();
        friendApply.setUserId(userId);
        friendApply.setTargetUserId(targetUserId);
        friendApply.setStatus(FriendConstant.APPLY_WAIT);
        friendApplyMapper.insert(friendApply);
    }
    @Transactional
    @Override
    public void handleRequest(Long applyId, Integer status, Long userId) {
        FriendApply apply = friendApplyMapper.selectById(applyId);
        if (apply == null || !apply.getTargetUserId().equals(userId)) {
            throw new RuntimeException("申请不存在或无权限操作");
        }
        if (!apply.getStatus().equals(FriendConstant.APPLY_WAIT)) {
            throw new RuntimeException("该申请已处理");
        }
        FriendApply updateApply = new FriendApply();
        updateApply.setId(applyId);
        updateApply.setStatus(status);
        friendApplyMapper.updateById(updateApply);
        //双向添加好友
        if (status.equals(FriendConstant.APPLY_AGREE)) {
            Friend me = new Friend();
            me.setUserId(userId);
            me.setFriendUserId(apply.getUserId());
            me.setGroupName(FriendConstant.DEFAULT_GROUP);
            friendMapper.insert(me);
            Friend him = new Friend();
            him.setUserId(apply.getUserId());
            him.setFriendUserId(userId);
            him.setGroupName(FriendConstant.DEFAULT_GROUP);
            friendMapper.insert(him);
        }
    }
    @Override
    public void updateGroup(Long friendId, Long userId, String groupName) {
        Friend friend = friendMapper.selectById(friendId);
        if (friend == null || !friend.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        Friend update = new Friend();
        update.setId(friendId);
        update.setGroupName(groupName);
        friendMapper.updateById(update);
    }
    @Override
    public Page<FriendApplyVO> listRequests(Integer pageNum, Integer pageSize, Long userId) {
        Page<FriendApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FriendApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendApply::getTargetUserId, userId);
        wrapper.eq(FriendApply::getStatus,FriendConstant.APPLY_WAIT);
        wrapper.orderByDesc(FriendApply::getCreateTime);
        Page<FriendApply> applyPage = friendApplyMapper.selectPage(page, wrapper);
        List<Long> userIds = applyPage.getRecords().stream()
                .map(FriendApply::getUserId)
                .toList();
        // 如果没有申请记录，直接返回空页面
        if (userIds.isEmpty()) {
            Page<FriendApplyVO> resultPage = new Page<>(pageNum, pageSize, 0);
            resultPage.setRecords(List.of());
            return resultPage;
        }
        List<User> userList = userMapper.selectBatchIds(userIds);
        List<FriendApplyVO> voList = applyPage.getRecords().stream().map(apply -> {
            User user = userList.stream()
                    .filter(u -> u.getId().equals(apply.getUserId()))//连表查询，也可以用自定义sql去避免使用filter
                    .findFirst().orElse(null);
            if (user == null) return null;
            FriendApplyVO vo = new FriendApplyVO();
            vo.setApplyId(apply.getId());
            vo.setApplyUserId(user.getId());
            vo.setApplyUsername(user.getUsername());
            vo.setStatus(apply.getStatus());
            vo.setCreateTime(apply.getCreateTime());
            return vo;
        }).filter(Objects::nonNull).toList();// 过滤掉找不到用户的数据
        Page<FriendApplyVO> resultPage = new Page<>(pageNum, pageSize, applyPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
    @Override
    public Page<FriendVO> getFriends(Integer pageNum, Integer pageSize, Long userId) {
        Page<Friend> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.orderByAsc(Friend::getGroupName); // 按分组排序
        wrapper.orderByDesc(Friend::getCreateTime);
        Page<Friend> friendPage = friendMapper.selectPage(page, wrapper);
        List<Long> friendIds = friendPage.getRecords().stream()
                .map(Friend::getFriendUserId)
                .toList();
        // 如果没有好友，直接返回空页面
        if (friendIds.isEmpty()) {
            Page<FriendVO> resultPage = new Page<>(pageNum, pageSize, 0);
            resultPage.setRecords(List.of());
            return resultPage;
        }
        List<User> userList = userMapper.selectBatchIds(friendIds);
        List<FriendVO> voList = friendPage.getRecords().stream().map(friend -> {
            User user = userList.stream()
                    .filter(u -> u.getId().equals(friend.getFriendUserId()))
                    .findFirst()//filter返回的是stream所以要findFirst
                    .orElse(null);
            if (user == null) return null;
            FriendVO vo = new FriendVO();
            vo.setFriendId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setAvatar(user.getAvatar());
            vo.setNickname(user.getNickname());
            vo.setMotto(user.getMotto());
            vo.setGroupName(friend.getGroupName());
            vo.setCreateTime(friend.getCreateTime());
            return vo;
        }).filter(Objects::nonNull)// 过滤掉找不到用户数据的数据
                .toList();
        Page<FriendVO> resultPage = new Page<>(pageNum, pageSize, friendPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }


}
