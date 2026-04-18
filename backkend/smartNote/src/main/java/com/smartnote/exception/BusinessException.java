package com.smartnote.exception;


public class BusinessException extends RuntimeException {
    // 传入异常提示信息
    public BusinessException(String message) {
        super(message);
    }
}
