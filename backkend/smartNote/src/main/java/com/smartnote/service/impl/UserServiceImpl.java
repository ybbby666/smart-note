package com.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.dto.UserProfileDTO;
import com.smartnote.exception.BusinessException;
import com.smartnote.mapper.UserMapper;
import com.smartnote.service.TokenBlacklistService;
import com.smartnote.service.UserService;
import com.smartnote.service.VerificationCodeService;
import com.smartnote.service.NoteFolderService;
import com.smartnote.utils.JwtUtils;
import com.smartnote.dto.UpdatePasswordDTO;
import com.smartnote.dto.UserLoginDTO;
import com.smartnote.dto.UserRegisterDTO;
import com.smartnote.dto.EmailLoginDTO;
import com.smartnote.dto.ResetPasswordDTO;
import com.smartnote.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.smartnote.pojo.User;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final cn.hutool.crypto.digest.BCrypt bCrypt = new cn.hutool.crypto.digest.BCrypt();

    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private NoteFolderService noteFolderService;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    private static final String DEFAULT_AVATAR = "/images/default-avatar.png";
    @Override
    public void register(UserRegisterDTO dto) {
        log.info("用户注册请求：用户名={}, 邮箱={}, 手机号={}", dto.getUsername(), dto.getEmail(), dto.getPhone());
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("用户注册失败：两次密码不一致，用户名={}", dto.getUsername());
            throw new BusinessException("两次密码不一致");
        }
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getVerificationCode(), "register")) {
            throw new BusinessException("验证码错误或已过期");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail()).or().eq(User::getPhone, dto.getPhone());
        if (count(wrapper) > 0) {
            log.warn("用户注册失败：邮箱/手机号已存在，邮箱={}, 手机号={}", dto.getEmail(), dto.getPhone());
            throw new BusinessException("邮箱或手机号已被注册");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(bCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getUsername());
        user.setAvatar(DEFAULT_AVATAR);
        save(user);
        noteFolderService.initDefaultFolder(user.getId());//一注册就生成文件夹
        verificationCodeService.removeCode(dto.getEmail(), "register");
        log.info("用户注册成功：用户名={}, 邮箱={}, 手机号={}", dto.getUsername(), dto.getEmail(), dto.getPhone());
    }
    @Override
    public Map<String, String> login(UserLoginDTO userLoginDTO) {
        log.info("用户请求登录");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail,userLoginDTO.getAccount())
                .or()
                .eq(User::getPhone,userLoginDTO.getAccount());
        
        // 使用 list() 避免 TooManyResultsException
        List<User> userList = list(wrapper);
        User user = null;
        if (userList != null && !userList.isEmpty()) {
            // 取第一条记录
            user = userList.get(0);
        }
        if (user == null) {
            throw new BusinessException("当前账户未创建，请前往注册");
        }
        if (!bCrypt.checkpw(userLoginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userName", user.getUsername());
        String accessToken = JwtUtils.generateAccessToken(claims);
        String refreshToken = JwtUtils.generateRefreshToken(claims);
        log.info("用户登录成功：用户ID={}, 用户名={}", user.getId(), user.getUsername());
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }
    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) {
        if(!updatePasswordDTO.getOldPassword().equals(updatePasswordDTO.getNewPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        User user = getById(userId);
        if(!bCrypt.checkpw(updatePasswordDTO.getOldPassword(),user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(bCrypt.hashpw(updatePasswordDTO.getNewPassword()));
        updateById(user);
        log.info("密码修改成功,userId={}", userId);
    }
    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        updateById(user);
    }

    @Override
    public void updateProfile(Long userId, UserProfileDTO dto) {
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setMotto(StringUtils.hasText(dto.getMotto()) ? dto.getMotto() : null);
        updateById(user);
    }
    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar() != null ? user.getAvatar() : DEFAULT_AVATAR);
        vo.setMotto(user.getMotto());
        return vo;
    }
    @Override
    public Map<String, String> emailLogin(EmailLoginDTO dto) {
        log.info("邮箱验证码登录请求：邮箱={}", dto.getEmail());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        
        // 使用 list() 避免 TooManyResultsException
        List<User> userList = list(wrapper);
        User user = null;
        if (userList != null && !userList.isEmpty()) {
            user = userList.get(0);
        }
        
        if (user == null) {
            throw new BusinessException("该邮箱未注册，请先注册");
        }
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getVerificationCode(), "login")) {
            throw new BusinessException("验证码错误或已过期");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userName", user.getUsername());
        verificationCodeService.removeCode(dto.getEmail(), "login");
        String accessToken = JwtUtils.generateAccessToken(claims);
        String refreshToken = JwtUtils.generateRefreshToken(claims);
        log.info("邮箱登录成功：用户ID={}, 用户名={}", user.getId(), user.getUsername());
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }
    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        log.info("重置密码请求：邮箱={}", dto.getEmail());
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getVerificationCode(), "reset")) {
            throw new BusinessException("验证码错误或已过期");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        
        // 使用 list() 避免 TooManyResultsException
        List<User> userList = list(wrapper);
        User user = null;
        if (userList != null && !userList.isEmpty()) {
            user = userList.get(0);
        }
        
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }
        user.setPassword(bCrypt.hashpw(dto.getNewPassword()));
        updateById(user);
        verificationCodeService.removeCode(dto.getEmail(), "reset");
        log.info("密码重置成功：邮箱={}", dto.getEmail());
    }
    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        log.info("刷新 Token 请求");
        if (!JwtUtils.validateToken(refreshToken)) {
            throw new BusinessException("Refresh Token 无效或已过期");
        }
        if (!JwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException("无效的 Token 类型");
        }
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BusinessException("Token 已被撤销");
        }
        Map<String, Object> claims = JwtUtils.extractClaimsFromRefreshToken(refreshToken);
        if (claims == null || claims.get("userId") == null) {
            throw new BusinessException("Token 解析失败");
        }
        String newAccessToken = JwtUtils.generateAccessToken(claims);
        String newRefreshToken = JwtUtils.generateRefreshToken(claims);
        long remainingTime = JwtUtils.getRemainingTime(refreshToken);
        tokenBlacklistService.addToBlacklist(refreshToken, remainingTime);
        log.info("Token 刷新成功，用户ID={}", claims.get("userId"));
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }
    @Override
    public void logout(String accessToken) {
        log.info("用户登出请求");
        if (accessToken != null && !accessToken.isEmpty()) {
            long remainingTime = JwtUtils.getRemainingTime(accessToken);
            tokenBlacklistService.addToBlacklist(accessToken, remainingTime);
            log.info("Access Token 已加入黑名单");
        }
        log.info("用户登出成功");
    }
}

