package com.smartnote.service.impl;

import com.smartnote.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    @Override
    public void addToBlacklist(String token, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + (expireSeconds * 1000);
        blacklist.put(token, expireTime);
        log.info("Token 已加入黑名单，将在 {} 秒后自动清理", expireSeconds);
    }
    @Override
    public boolean isBlacklisted(String token) {
        Long expireTime = blacklist.get(token);
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            blacklist.remove(token);
            log.debug("Token 已过期，从黑名单中移除");
            return false;
        }
        return true;
    }
    @Override
    public void removeFromBlacklist(String token) {
        blacklist.remove(token);
        log.debug("Token 已从黑名单中手动移除");
    }
    @Override
    public int getBlacklistSize() {
        return blacklist.size();
    }
    @Scheduled(fixedRate = 1800000) // 30分钟
    @Override
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        int beforeSize = blacklist.size();
        blacklist.entrySet().removeIf(entry -> now > entry.getValue());
        int afterSize = blacklist.size();
        int cleaned = beforeSize - afterSize;
        if (cleaned > 0) {
            log.info("清理过期 Token：{} 个，当前黑名单大小：{}", cleaned, afterSize);
        }
    }
}
