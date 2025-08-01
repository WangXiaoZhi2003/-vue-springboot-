package com.code.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 12:54
 */
@Component
public class JwtInterceptor extends OncePerRequestFilter{

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        // 放行注册、登录和 WebSocket 连接
        if (path.startsWith("/api/users/register") ||
                path.startsWith("/api/users/login") ||
                path.startsWith("/ws/")) { // 放行 WebSocket 路径
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");
        if (token == null || jwtUtil.getEmail(token) == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        token.replace("Bearer ", "");
        // 提取用户名
        String email = jwtUtil.getEmail(token);
        // 创建 Authentication 对象并放入上下文
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
