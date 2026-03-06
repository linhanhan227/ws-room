package com.chat.config;

import com.chat.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    @Autowired
    public ChatHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String sessionId = UUID.randomUUID().toString();
        attributes.put("sessionId", sessionId);
        sessions.put(sessionId, sessionId);

        String query = request.getURI().getQuery();
        if (query != null) {
            String token = extractParameter(query, "token");
            if (token != null && !token.isEmpty()) {
                token = URLDecoder.decode(token, StandardCharsets.UTF_8);
                
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    String userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);
                    boolean isAdmin = jwtUtil.isAdminFromToken(token);
                    
                    attributes.put("token", token);
                    attributes.put("userId", userId);
                    attributes.put("username", username);
                    attributes.put("isAdmin", isAdmin);
                    attributes.put("authenticated", true);
                    
                    return true;
                }
            }
        }

        attributes.put("authenticated", false);
        return true;
    }

    private String extractParameter(String query, String paramName) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
