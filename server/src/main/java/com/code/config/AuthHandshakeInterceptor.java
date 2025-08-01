// AuthHandshakeInterceptor.java
package com.code.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public AuthHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {



        // 从查询参数获取 token
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) return false;

        String token = query.split("token=")[1].split("&")[0];
        token = token.replace("Bearer ", "");

        try {
            // 验证 token 并获取邮箱
            String email = jwtUtil.getEmail(token);
            if (email != null) {
                attributes.put("email", email);
                return true;
            }
        } catch (Exception e) {
            // Token 无效
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
