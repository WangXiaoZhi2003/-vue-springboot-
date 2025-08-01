package com.code.project.controller;

import com.code.project.domain.UserDTO;
import com.code.project.service.UserService;
import com.code.web.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:14
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody UserDTO userDTO) {
        userService.register(userDTO.getEmail(), userDTO.getPassword());
        return ApiResponse.success("注册成功");
        //return ResponseEntity.ok(Map.of("code", 200, "message", "注册成功"));
    }



    @PostMapping("/repasswd")
    public ApiResponse<?> repasswd(@RequestBody Map<String, String> passwordMap) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(
                email,
                passwordMap.get("oldPassword"),
                passwordMap.get("newPassword")
        );
        return ApiResponse.success("密码修改成功");
    }
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody UserDTO userDTO) {
        String token = userService.login(userDTO.getEmail(), userDTO.getPassword());

        // 直接构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        // 创建用户信息对象 - 只包含用户名（邮箱）
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", userDTO.getEmail());
        data.put("user", userInfo);

        return ApiResponse.success(data);
    }

    @PostMapping("/rules")
    public ApiResponse<?> setFilterRules(@RequestBody Map<String, String> request) {
        // 获取当前登录用户
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 处理请求参数
        String forbiddenKeywords = request.get("forbiddenKeywords");

        // 调用服务层
        userService.setFilterRules(email, forbiddenKeywords);

        return ApiResponse.success("过滤规则设置成功");
    }


}
