package com.smartnote.intercepter;

import com.smartnote.service.NotePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
@RequiredArgsConstructor
public class NotePermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private NotePermissionService notePermissionService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //仅拦截查看和编辑
        String requestURI = request.getRequestURI();
        if (!requestURI.startsWith("/notes/") || requestURI.contains("/permission")) {
            return true;
        }
        String[] parts = requestURI.split("/");
        Long noteId;
        try {
            noteId = Long.parseLong(parts[2]);
        } catch (Exception e) {
            return true;
        }
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            if (notePermissionService.canRead(noteId, null)) {
                return true;
            } else {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("无权限访问");
                return false;
            }
        }
        if (!notePermissionService.canRead(noteId, userId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("无权限访问该笔记");
            return false;
        }
        if (request.getMethod().equals("PUT") || request.getMethod().equals("POST")) {
            if (!notePermissionService.canEdit(noteId, userId)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("无权限编辑该笔记");
                return false;
            }
        }
        return true;
    }

}
