package com.smartnote.service;

import java.util.Set;

/**
 * Token 黑名单服务
 * 用于管理已登出或失效的 Token
 */
public interface TokenBlacklistService {
    
    /**
     * 将 Token 加入黑名单
     * @param token JWT Token
     * @param expireSeconds 过期时间（秒），应与 Token 剩余有效期一致
     */
    void addToBlacklist(String token, long expireSeconds);
    
    /**
     * 检查 Token 是否在黑名单中
     * @param token JWT Token
     * @return true-在黑名单中，false-不在黑名单中
     */
    boolean isBlacklisted(String token);
    
    /**
     * 从黑名单中移除 Token（通常不需要，Token 过期后会自动清理）
     * @param token JWT Token
     */
    void removeFromBlacklist(String token);
    
    /**
     * 获取黑名单大小
     * @return 黑名单中的 Token 数量
     */
    int getBlacklistSize();
    
    /**
     * 清理已过期的黑名单项
     */
    void cleanExpiredTokens();
}
