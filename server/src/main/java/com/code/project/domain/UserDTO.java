package com.code.project.domain;

import lombok.Data;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:01
 */
@Data
public class UserDTO {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
