package com.smartnote.intercepter;

import com.smartnote.service.TokenBlacklistService;
import com.smartnote.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenIntercepter implements HandlerInterceptor {
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        // 放行公开接口（包括刷新 Token 和登出）
        if (isPublicPath(requestURI)) {
            log.debug("放行公开接口: {}", requestURI);
            return true;
        }
        // 从请求头获取 Token
        String token = request.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        if (token == null || token.trim().isEmpty()) {
            log.warn("令牌为空，URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        try {
            Claims claims = JwtUtils.parseToken(token);
            // 检查 Token 是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("令牌已在黑名单中");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"令牌已失效，请重新登录\"}");
                return false;
            }
            // 提取 userId
            Object userId = claims.get("userId");
            if (userId == null) {
                log.warn("令牌缺少userId字段");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"令牌格式错误\"}");
                return false;
            }
            // 设置到 request 属性中，供后续使用
            request.setAttribute("userId", Long.valueOf(userId.toString()));
            log.debug("令牌验证通过，userId={}, URI={}", userId, requestURI);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("令牌已过期");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("令牌格式错误");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌格式错误\"}");
            return false;
        } catch (Exception e) {
            log.warn("令牌验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效\"}");
            return false;
        }
    }
    private boolean isPublicPath(String requestURI) {
        return requestURI.equals("/users/login") ||
               requestURI.equals("/users/register") ||
               requestURI.equals("/users/email-login") ||
               requestURI.equals("/users/verification-code") ||
               requestURI.equals("/users/password/reset") ||
               requestURI.equals("/users/token/refresh") ||
               requestURI.equals("/users/logout") ||
               requestURI.startsWith("/images/");
    }
}
