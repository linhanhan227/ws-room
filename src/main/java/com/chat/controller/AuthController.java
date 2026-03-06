package com.chat.controller;

import com.chat.exception.AuthenticationException;
import com.chat.exception.ParameterException;
import com.chat.model.ErrorCode;
import com.chat.model.User;
import com.chat.service.UserService;
import com.chat.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isEmpty()) {
            throw new ParameterException(ErrorCode.PARAM_EMPTY, "用户名不能为空");
        }

        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            throw new AuthenticationException(ErrorCode.AUTH_USERNAME_PASSWORD_ERROR);
        }

        if (password == null || password.isEmpty() || !user.getPassword().equals(password)) {
            throw new AuthenticationException(ErrorCode.AUTH_USERNAME_PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getAdmin());

        return ResponseEntity.ok(Map.of(
                "message", "登录成功",
                "token", token,
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "isAdmin", user.getAdmin()
        ));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ParameterException(ErrorCode.PARAM_INVALID, "无效的授权头");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        boolean isAdmin = jwtUtil.isAdminFromToken(token);

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "userId", userId,
                "username", username,
                "isAdmin", isAdmin
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ParameterException(ErrorCode.PARAM_INVALID, "无效的授权头");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        String username = jwtUtil.getUsernameFromToken(token);

        return ResponseEntity.ok(Map.of(
                "message", "退出登录成功",
                "username", username
        ));
    }
}
