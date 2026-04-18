package com.smartnote.service;

public interface VerificationCodeService {

    void sendVerificationCode(String email, String type);

    boolean verifyCode(String email, String code, String type);

    void removeCode(String email, String type);
}
