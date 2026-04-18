package com.smartnote.controller;

import com.smartnote.dto.UserProfileDTO;
import com.smartnote.pojo.Result;
import com.smartnote.service.UserService;
import com.smartnote.service.VerificationCodeService;
import com.smartnote.dto.UpdatePasswordDTO;
import com.smartnote.dto.UserLoginDTO;
import com.smartnote.dto.UserRegisterDTO;
import com.smartnote.dto.SendVerificationCodeDTO;
import com.smartnote.dto.EmailLoginDTO;
import com.smartnote.dto.ResetPasswordDTO;
import com.smartnote.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;
    @PostMapping("/register")
    public Result register(@Valid @RequestBody UserRegisterDTO userRegisterDTO ) {
        userService.register(userRegisterDTO);
        log.info("注册用户");
        return Result.success();
    }

    @PostMapping("/login")
    public Result login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        Map<String, String> tokens = userService.login(userLoginDTO);
        log.info("用户登录成功");
        return Result.success(tokens);
    }

    @PostMapping("/login/email")
    public Result emailLogin(@Valid @RequestBody EmailLoginDTO dto) {
        Map<String, String> tokens = userService.emailLogin(dto);
        log.info("邮箱验证码登录：邮箱={}", dto.getEmail());
        return Result.success(tokens);
    }
    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "token", required = false) String token,
                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        // 支持两种 Token 传递方式
        String accessToken = token;
        if (accessToken == null && authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        userService.logout(accessToken);
        log.info("用户登出");
        return Result.success("登出成功");
    }

    @PostMapping("/token/refresh")
    public Result refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        //StringUtils.hasText其实也可以代替两个功能
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error("Refresh Token 不能为空");
        }
        Map<String, String> tokens = userService.refreshToken(refreshToken);
        log.info("Token 刷新成功");
        return Result.success(tokens);
    }
    @GetMapping("/profile")
    public Result getMyProfile(@RequestAttribute("userId") Long userId) {
        UserProfileVO vo = userService.getProfile(userId);
        return Result.success(vo);
    }
    @GetMapping("/{userId}/profile")
    public Result getProfile(@PathVariable Long userId) {
        UserProfileVO vo = userService.getProfile(userId);
        return Result.success(vo);
    }
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody UserProfileDTO dto,
                                @RequestAttribute("userId") Long userId) {
        userService.updateProfile(userId, dto);
        log.info("用户{}更新资料", userId);
        return Result.success();
    }
    @PutMapping("/password")
    public Result updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO,
                                 @RequestAttribute("userId") Long userId) {
        userService.updatePassword(userId, updatePasswordDTO);
        log.info("用户{}修改密码", userId);
        return Result.success();
    }
    @PostMapping("/verification-code")
    public Result sendVerificationCode(@Validated @RequestBody SendVerificationCodeDTO dto) {
        verificationCodeService.sendVerificationCode(dto.getEmail(), dto.getType());
        log.info("发送验证码：邮箱={}, 类型={}", dto.getEmail(), dto.getType());
        return Result.success("验证码已发送");
    }
    @PostMapping("/password/reset")
    public Result resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        log.info("重置密码：邮箱={}", dto.getEmail());
        return Result.success("密码重置成功");
    }
}
