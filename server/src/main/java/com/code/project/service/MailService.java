package com.code.project.service;

import com.code.project.domain.Mail;
import com.code.project.domain.MailDTO;

import java.util.List;
import java.util.Map;

public interface MailService {
    public void toggleStar(String email, Long mailId);
    public void sendMail(String fromEmail, MailDTO dto);
    Mail getMailDetail(String email, Long mailId);
    public List<Mail> getInbox(String toEmail);
    void deleteMail(String email, Long mailId);
    public List<Mail> getSent(String fromEmail);
    List<Mail> getDeleted(String email);
    public List<Mail> search(String email, String keyword);
    // 新增草稿保存方法
    void saveDraft(String fromEmail, MailDTO dto);
    // 新增获取草稿邮件方法声明
    List<Mail> getDrafts(String email);
    // 新增获取垃圾邮件方法声明
    List<Mail> getSpam(String email);

    public void markAsRead(String email, Long mailId);

    // 新增接口声明
    Map<String, Long> getMailStats(String email);
    List<Mail> getImportantMails(String email);
    List<Mail> getUnreadSummary(String email);

}
