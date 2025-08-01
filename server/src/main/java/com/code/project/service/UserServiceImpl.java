package com.code.project.service;

import com.code.config.JwtUtil;
import com.code.project.domain.User;
import com.code.project.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:12
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("用户已存在");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return jwtUtil.generateToken(email);
    }
    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void setFilterRules(String email, String forbiddenKeywords) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证关键词格式（可选）
        if(forbiddenKeywords != null && forbiddenKeywords.length() > 255) {
            throw new RuntimeException("关键词长度超过限制");
        }

        user.setForbiddenKeywords(forbiddenKeywords);
        userRepository.save(user);
    }

}
