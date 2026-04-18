package com.smartnote.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Component
public class JwtUtils {
    private static SecretKey KEY;
    private static long ACCESS_TOKEN_EXPIRATION;
    private static long REFRESH_TOKEN_EXPIRATION;
    // 通过构造函数注入，支持从配置文件读取
    public JwtUtils(
            @Value("${jwt.secret:a1b2c3d4e5f67890abcdef1234567890a1b2c3d4e5f67890}") String secret,
            @Value("${jwt.expiration:43200000}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpiration) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT密钥长度至少为32字符");
        }
        KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));//将配置的密钥字符串）转换为字节数组
        ACCESS_TOKEN_EXPIRATION = accessTokenExpiration;
        REFRESH_TOKEN_EXPIRATION = refreshTokenExpiration;
        log.info("JWT工具类初始化成功，Access Token过期时间: {}小时, Refresh Token过期时间: {}天", 
                accessTokenExpiration / 3600000, refreshTokenExpiration / 86400000);
    }
    public static String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, ACCESS_TOKEN_EXPIRATION, "access");
    }
    public static String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, REFRESH_TOKEN_EXPIRATION, "refresh");
    }
    private static String generateToken(Map<String, Object> claims, long expiration, String type) {
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put("type", type);
        tokenClaims.put("jti", UUID.randomUUID().toString()); // 唯一ID
        return Jwts.builder()
                .signWith(KEY)
                .addClaims(tokenClaims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .compact();
    }
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    public static boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    public static Map<String, Object> extractClaimsFromRefreshToken(String refreshToken) {
        try {
            Claims claims = parseToken(refreshToken);
            Map<String, Object> userClaims = new HashMap<>();
            userClaims.put("userId", claims.get("userId"));
            userClaims.put("userName", claims.get("userName"));
            return userClaims;
        } catch (Exception e) {
            log.error("从 Refresh Token 提取信息失败: {}", e.getMessage());
            return null;
        }
    }
    public static long getRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}
