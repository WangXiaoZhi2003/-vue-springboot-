package com.code.project.service;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:11
 */
public interface UserService {

    public void register(String email, String password);

    public String login(String email, String password);

    void changePassword(String email, String oldPassword, String newPassword);

    void setFilterRules(String email, String forbiddenKeywords); // 新增设置规则方法
}
