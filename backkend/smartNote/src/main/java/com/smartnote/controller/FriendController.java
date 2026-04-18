package com.smartnote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartnote.pojo.Result;
import com.smartnote.service.FriendService;
import com.smartnote.vo.FriendApplyVO;
import com.smartnote.vo.FriendVO;
import com.smartnote.dto.UserSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/friend")
public class FriendController {
    @Autowired
    private FriendService friendService;
    @PostMapping("/friend")
    public Result searchFriend( @RequestParam String keyword,
                                @RequestAttribute Long userId){
        UserSearchDTO userSearchDTO = new UserSearchDTO();
        userSearchDTO.setKeyword(keyword);
        FriendVO friendVO = new FriendVO();
        friendVO=friendService.searchFriend(userSearchDTO,userId);
        return Result.success(friendVO);
    }
    @PostMapping("/requests/{targetUserId}")
    public Result sentRequest(@RequestAttribute Long userId,@PathVariable Long targetUserId){
        friendService.sentRequest(userId,targetUserId);
        log.info("{}向{}发送好友申请",userId,targetUserId);
        return Result.success();
    }
    @PutMapping("/requests/{applyId}")
    public Result handleRequests(@RequestAttribute Long userId,@PathVariable Long applyId,@RequestParam Integer status){
        friendService.handleRequest(applyId, status, userId);
        log.info("用户{}将{}修改为{}",userId,applyId,status);
        return Result.success();
    }
    @GetMapping
    public Result getFriends(@RequestAttribute Long userId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        Page<FriendVO> page = friendService.getFriends(pageNum, pageSize, userId);
        log.info("查询朋友");
        return Result.success(page);
    }
    @PutMapping("/{friendId}/group")
    public Result updateGroup(@PathVariable Long friendId,@RequestParam String groupName,@RequestAttribute Long userId){
        friendService.updateGroup(friendId,userId,groupName);
        log.info("修改分组");
        return Result.success();
    }
    @GetMapping("/requests")
    public Result listRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute Long userId
    ) {
        Page<FriendApplyVO> page = friendService.listRequests(pageNum, pageSize, userId);
        log.info("查询申请列表");
        return Result.success(page);
    }


}
