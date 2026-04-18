package com.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.dto.UserProfileDTO;
import com.smartnote.pojo.User;
import com.smartnote.dto.UpdatePasswordDTO;
import com.smartnote.dto.UserLoginDTO;
import com.smartnote.dto.UserRegisterDTO;
import com.smartnote.dto.EmailLoginDTO;
import com.smartnote.dto.ResetPasswordDTO;
import com.smartnote.vo.UserProfileVO;

import java.util.Map;

public interface UserService extends IService<User> {
    void  register(UserRegisterDTO userRegisterDTO);

    Map<String, String> login(UserLoginDTO userLoginDTO);

    void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO);

    void updateAvatar(Long userId, String avatarUrl);

    void updateProfile(Long userId, UserProfileDTO dto);

    UserProfileVO getProfile(Long userId);

    /**
     * 邮箱验证码登录
     */
    Map<String, String> emailLogin(EmailLoginDTO dto);

    /**
     * 邮箱验证码重置密码
     */
    void resetPassword(ResetPasswordDTO dto);
    
    /**
     * 刷新 Token
     */
    Map<String, String> refreshToken(String refreshToken);
    
    /**
     * 登出（将 Token 加入黑名单）
     */
    void logout(String accessToken);
}
