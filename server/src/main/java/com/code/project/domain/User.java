package com.code.project.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:03
 */
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;

    private String password;

    private String forbiddenKeywords; // 新增过滤规则字段

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
    public String getForbiddenKeywords() {
        return forbiddenKeywords;
    }

    public void setForbiddenKeywords(String forbiddenKeywords) {
        this.forbiddenKeywords = forbiddenKeywords;
    }
}
