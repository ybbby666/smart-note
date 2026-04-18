package com.smartnote.service.impl;
import com.smartnote.exception.BusinessException;
import com.smartnote.service.VerificationCodeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Autowired
    private JavaMailSender mailSender;
    private final ConcurrentHashMap<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    @Override
    public void sendVerificationCode(String email, String type) {
        validateType(type);
        String key = buildKey(type, email);
        checkSendFrequency(key);
        String code = generateCode();
        // 存储到内存，记录发送时间
        CodeInfo codeInfo = new CodeInfo();
        codeInfo.setCode(code);
        codeInfo.setExpireTime(System.currentTimeMillis() + CODE_EXPIRE_MINUTES * 60 * 1000);
        codeInfo.setSendTime(System.currentTimeMillis());
        codeStore.put(key, codeInfo);
        sendEmail(email, code, type);
        log.info("验证码发送成功：邮箱={}, 类型={}", email, type);
    }
    @Override
    public boolean verifyCode(String email, String code, String type) {
        validateType(type);
        String key = buildKey(type, email);
        CodeInfo codeInfo = codeStore.get(key);
        if (codeInfo == null) {
            log.warn("验证码不存在或已过期：邮箱={}, 类型={}", email, type);
            return false;
        }
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.getExpireTime()) {
            log.warn("验证码已过期：邮箱={}, 类型={}", email, type);
            codeStore.remove(key);
            return false;
        }
        boolean isValid = codeInfo.getCode().equals(code);
        if (isValid) {
            log.info("验证码验证成功：邮箱={}, 类型={}", email, type);
        } else {
            log.warn("验证码错误：邮箱={}, 类型={}", email, type);
        }
        return isValid;
    }
    @Override
    public void removeCode(String email, String type) {
        String key = buildKey(type, email);
        codeStore.remove(key);
        log.info("验证码已删除：邮箱={}, 类型={}", email, type);
    }
    private String buildKey(String type, String email) {
        return type + ":" + email;
    }
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    private void validateType(String type) {
        if (!"register".equals(type) && !"login".equals(type) && !"reset".equals(type)) {
            throw new BusinessException("无效的验证码类型");
        }
    }
    private void checkSendFrequency(String key) {
        CodeInfo codeInfo = codeStore.get(key);
        if (codeInfo != null) {
            long currentTime = System.currentTimeMillis();
            long elapsed = (currentTime - codeInfo.getSendTime()) / 1000;
            if (elapsed < SEND_INTERVAL_SECONDS) {
                long remaining = SEND_INTERVAL_SECONDS - elapsed;
                throw new BusinessException("发送过于频繁，请" + remaining + "秒后再试");
            }
        }
    }
    private void sendEmail(String email, String code, String type) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Smart Note <2039578996@qq.com>");
            message.setTo(email);
            String subject;
            String content;
            switch (type) {
                case "register":
                    subject = "Smart Note 注册验证码";
                    content = String.format(
                            "欢迎注册 Smart Note！\n\n" +
                            "您的验证码是：%s\n\n" +
                            "验证码有效期为 %d 分钟，请勿泄露给他人。\n\n" +
                            "如非本人操作，请忽略此邮件。",
                            code, CODE_EXPIRE_MINUTES
                    );
                    break;
                case "login":
                    subject = "Smart Note 登录验证码";
                    content = String.format(
                            "您正在登录 Smart Note！\n\n" +
                            "您的验证码是：%s\n\n" +
                            "验证码有效期为 %d 分钟，请勿泄露给他人。\n\n" +
                            "如非本人操作，请忽略此邮件。",
                            code, CODE_EXPIRE_MINUTES
                    );
                    break;
                case "reset":
                    subject = "Smart Note 密码重置验证码";
                    content = String.format(
                            "您正在重置 Smart Note 密码！\n\n" +
                            "您的验证码是：%s\n\n" +
                            "验证码有效期为 %d 分钟，请勿泄露给他人。\n\n" +
                            "如非本人操作，请忽略此邮件。",
                            code, CODE_EXPIRE_MINUTES
                    );
                    break;
                default:
                    throw new BusinessException("无效的验证码类型");
            }
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("验证码邮件发送成功：邮箱={}, 类型={}", email, type);
        } catch (Exception e) {
            log.error("验证码邮件发送失败：邮箱={}, 类型={}", email, type, e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }
    }
    @Data
    private static class CodeInfo {
        private String code;
        private long expireTime;
        private long sendTime;
    }
}
