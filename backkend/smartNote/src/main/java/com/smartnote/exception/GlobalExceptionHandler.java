package com.smartnote.exception;

import com.smartnote.pojo.Result;
import com.smartnote.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常统一拦截
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 捕获所有异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        e.printStackTrace(); // 后台打印日志
        return Result.error("服务器异常，请稍后重试");
    }
    // 捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }
    // 捕获JWT异常
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public Result handleJwtException() {
        return Result.error("登录已过期或令牌无效");
    }
    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败：{}", msg);
        return Result.error(msg);
    }
}
