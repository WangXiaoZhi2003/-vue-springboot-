package com.code.config;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class MailWebSocketHandler extends TextWebSocketHandler {
    // 存储用户会话 {email: session}
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String email = (String) session.getAttributes().get("email");
        if (email != null) {
            sessions.put(email, session);
            System.out.println("WebSocket 连接建立: " + email);
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String email = (String) session.getAttributes().get("email");
        if (email != null) {
            sessions.remove(email);
            System.out.println("WebSocket 连接关闭: " + email);
        }
    }
    // 发送新邮件通知
    public void sendNewMailNotification(String email, String from, String subject) {
        WebSocketSession session = sessions.get(email);
        if (session != null && session.isOpen()) {
            try {
                String json = String.format(
                        "{\"type\":\"NEW_MAIL\",\"from\":\"%s\",\"subject\":\"%s\"}",
                        from, subject
                );
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                System.err.println("发送通知失败: " + e.getMessage());
            }
        }
    }
}