package com.smartnote.controller;

import com.smartnote.exception.BusinessException;
import com.smartnote.pojo.Result;
import com.smartnote.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@RestController
@Slf4j
public class UploadController {
    @Autowired
    private UserService userService;
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("接收图片");
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "." + originalFilename.substring(originalFilename.lastIndexOf("."));
        file.transferTo(new File("D:/images/" + fileName));
        Result result = Result.success(fileName);
        log.info("返回：{}", result);
        return result;
    }
    @PostMapping("/avatar")
    public Result uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute Long userId) throws IOException {

        log.info("用户{}上传头像", userId);

        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = "avatar_" + userId + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + ext;
        java.io.File dir = new java.io.File("D:/images/avatar/" + userId);
        if (!dir.exists()) dir.mkdirs();
        file.transferTo(new java.io.File(dir, fileName));
        String avatarUrl = "http://localhost:8080/images/avatar/" + userId + "/" + fileName;
        userService.updateAvatar(userId, avatarUrl);
        return Result.success(avatarUrl);
    }
}
