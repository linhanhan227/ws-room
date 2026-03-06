package com.chat.interceptor;

import com.chat.annotation.AdminRequired;
import com.chat.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AdminRequired adminRequired = handlerMethod.getMethodAnnotation(AdminRequired.class);

        if (adminRequired != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"缺少授权令牌，请在请求头中添加 Authorization: Bearer <token>\"}");
                return false;
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Token无效或已过期\"}");
                return false;
            }

            boolean isAdmin = jwtUtil.isAdminFromToken(token);
            if (!isAdmin) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"权限不足，需要管理员权限\"}");
                return false;
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
        }

        return true;
    }
}
