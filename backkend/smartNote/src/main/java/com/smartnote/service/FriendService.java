package com.smartnote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.pojo.Friend;
import com.smartnote.vo.FriendApplyVO;
import com.smartnote.vo.FriendVO;
import com.smartnote.dto.UserSearchDTO;

public interface FriendService extends IService<Friend> {
    FriendVO searchFriend(UserSearchDTO userSearchDTO, Long userId);

    void sentRequest(Long userId, Long targetUserId);

    void handleRequest(Long applyId, Integer status, Long userId);

   

    void updateGroup(Long friendId, Long userId, String groupName);


    Page<FriendApplyVO> listRequests(Integer pageNum, Integer pageSize, Long userId);

    Page<FriendVO> getFriends(Integer pageNum, Integer pageSize, Long userId);
}
