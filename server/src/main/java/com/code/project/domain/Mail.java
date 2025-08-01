package com.code.project.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:02
 */
@Entity
@Table(name = "mail_info")
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "[from]")
    private String from;

    @Column(name = "[to]")
    private String to;

    private String subject;
    private String content;

    @Column(name = "[read]")
    private Integer read;
    @Column(name = "mail_type")
    private String mailType;// "inbox"（收件箱）或 "sent"（已发送）
    private LocalDateTime timestamp;

    @Column(name = "attachment_paths")
    private String attachmentPaths;

    @Column(name = "starred")
    private Boolean starred;
    // 修改getter/setter
    public String getMailType() {
        return mailType;
    }
    public void setMailType(String mailType) {
        this.mailType = mailType;
    }
    // 新增status字段
    private String status;
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    // 新增 show 字段
    @Column(name = "`show`") // 使用反引号转义关键字
    private Integer show; // 0=可见，1=已删除

    // Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRead() {
        return read;
    }

    public void setRead(Integer read) {
        this.read = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAttachmentPaths() {
        return attachmentPaths;
    }

    public void setAttachmentPaths(String attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    // show 字段的 getter 和 setter
    public Integer getShow() {
        return show;
    }

    public void setShow(Integer show) {
        this.show = show;
    }
}
