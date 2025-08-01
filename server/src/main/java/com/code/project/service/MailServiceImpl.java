package com.code.project.service;

import com.code.config.MailWebSocketHandler;
import com.code.project.domain.Mail;
import com.code.project.domain.MailDTO;
import com.code.project.domain.MailStats;
import com.code.project.domain.User;
import com.code.project.mapper.MailRepository;
import com.code.project.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:08
 */
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailWebSocketHandler mailWebSocketHandler;

    @Override
    public void sendMail(String fromEmail, MailDTO dto) {
        // 1. 获取收件人的过滤规则
        String forbiddenKeywords = "";
        Optional<User> receiverOptional = userRepository.findByEmail(dto.getReceiverEmail());
        if (receiverOptional.isPresent()) {
            User receiver = receiverOptional.get();
            forbiddenKeywords = receiver.getForbiddenKeywords() != null ?
                    receiver.getForbiddenKeywords() : "";
        }
        // 2. 检查邮件内容是否触发过滤规则
        boolean isSpam = false;
        if (!forbiddenKeywords.isEmpty()) {
            String[] keywordArray = forbiddenKeywords.split(",");
            String mailContent = (dto.getSubject() + " " + dto.getContent()).toLowerCase();

            for (String keyword : keywordArray) {
                String cleanKeyword = keyword.trim().toLowerCase();
                if (!cleanKeyword.isEmpty() && mailContent.contains(cleanKeyword)) {
                    isSpam = true;
                    break; // 发现一个关键词即可标记为垃圾邮件
                }
            }
        }
        // 3. 收件人邮件记录（显示在收件箱）
        Mail receiverMail = new Mail();
        receiverMail.setFrom(fromEmail);
        receiverMail.setTo(dto.getReceiverEmail());
        receiverMail.setSubject(dto.getSubject());
        receiverMail.setContent(dto.getContent());
        receiverMail.setTimestamp(LocalDateTime.now());
        receiverMail.setRead(0);
        receiverMail.setStarred(false);
        receiverMail.setShow(0);
        receiverMail.setMailType("inbox");
        receiverMail.setStatus(isSpam ? "spam" : "normal"); // 关键修改：根据检测结果设置状态
        if (dto.getAttachmentPaths() != null) {
            receiverMail.setAttachmentPaths(String.join(",", dto.getAttachmentPaths()));
        } else {
            receiverMail.setAttachmentPaths("");
        }
        mailRepository.save(receiverMail);
        // 4. 发件人邮件记录（显示在已发送）- 始终标记为正常
        Mail senderMail = new Mail();
        senderMail.setFrom(fromEmail);
        senderMail.setTo(dto.getReceiverEmail());
        senderMail.setSubject(dto.getSubject());
        senderMail.setContent(dto.getContent());
        senderMail.setTimestamp(LocalDateTime.now());
        senderMail.setRead(0);
        senderMail.setStarred(false);
        senderMail.setShow(0);
        senderMail.setMailType("sent");
        senderMail.setStatus("normal"); // 发件人侧始终显示为正常
        if (dto.getAttachmentPaths() != null) {
            senderMail.setAttachmentPaths(String.join(",", dto.getAttachmentPaths()));
        } else {
            senderMail.setAttachmentPaths("");
        }
        mailRepository.save(senderMail);

        if ("normal".equals(receiverMail.getStatus())) {
            mailWebSocketHandler.sendNewMailNotification(
                    dto.getReceiverEmail(),
                    fromEmail,
                    dto.getSubject()
            );
        }


    }


    @Override
    public void saveDraft(String fromEmail, MailDTO dto) {
        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setTo(dto.getReceiverEmail());
        mail.setSubject(dto.getSubject());
        mail.setContent(dto.getContent());
        mail.setTimestamp(LocalDateTime.now());
        mail.setRead(0);
        mail.setStarred(false);
        mail.setShow(0);
        mail.setMailType(null);  // 关键修复：草稿属于发件人视图
        mail.setStatus("draft");
        mail.setAttachmentPaths("");
        mailRepository.save(mail);
    }

    // 删除邮件方法实现
    @Override
    public void deleteMail(String email, Long mailId) {
        Mail mail = mailRepository.findById(mailId)
                .orElseThrow(() -> new RuntimeException("邮件不存在"));

        // 验证用户权限：必须是发件人或收件人
        if (!email.equals(mail.getFrom()) && !email.equals(mail.getTo())) {
            throw new RuntimeException("无权操作此邮件");
        }

        // 逻辑删除：设置 show 为 1
        mail.setShow(1);
        mailRepository.save(mail);
    }
    @Override
    public List<Mail> getDeleted(String email) {
        return mailRepository.findDeletedMails(email);
    }
    @Override
    public List<Mail> getInbox(String toEmail) {
        // 使用新的查询方法，只返回状态为'inbox'的邮件
        return mailRepository.findInboxMails(toEmail, 0);
    }


    @Override
    public Mail getMailDetail(String email, Long mailId) {
        Optional<Mail> mailOpt = mailRepository.findByIdAndShowEquals(mailId, 0); // 修改：使用新的查询方法

        // 检查邮件是否存在且属于当前用户
        if (mailOpt.isPresent()) {
            Mail mail = mailOpt.get();
            if (email.equals(mail.getFrom()) || email.equals(mail.getTo())) {
                return mail;
            }
        }
        return null;
    }
    @Override
    public List<Mail> getSpam(String email) {
        return mailRepository.findByToAndStatusAndShowEquals(email, "spam", 0);
    }

    @Override
    public List<Mail> getSent(String fromEmail) {
        // 使用新的查询方法排除草稿
        return mailRepository.findSentMailsExcludingDrafts(fromEmail, 0);
    }
    @Override
    public List<Mail> getDrafts(String email) {
        return mailRepository.findMailByFromAndStatusAndShowEquals(email, "draft", 0);
    }

    @Override
    public List<Mail> search(String email, String keyword) {
        return mailRepository.searchByKeyword(email, keyword); // 注意：Repository中已修改SQL
    }

    @Override
    public void markAsRead(String email, Long mailId) {
        Mail mail = mailRepository.findMailByIdAndToAndShowEquals(mailId, email, 0) // 修改：加入show=0条件
                .orElseThrow(() -> new RuntimeException("邮件不存在"));
        mail.setRead(1);
        mailRepository.save(mail);
    }

    /**
     * 切换邮件星标状态
     * @param email 用户邮箱
     * @param mailId 邮件ID
     */
    public void toggleStar(String email, Long mailId) {
        Mail mail = mailRepository.findByIdAndShowEquals(mailId, 0) // 修改：使用新的查询方法
                .orElseThrow(() -> new RuntimeException("邮件不存在"));

        // 切换星标状态
        mail.setStarred(!mail.getStarred());
        mailRepository.save(mail);
    }

    @Override
    public Map<String, Long> getMailStats(String email) {
        MailStats stats = mailRepository.getMailStats(email);
        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", stats.getUnreadCount());
        result.put("starredCount", stats.getStarredCount());
        result.put("sentCount", stats.getSentCount());
        result.put("draftCount", stats.getDraftCount());
        return result;
    }
    // 处理不同类型的数值转换
    private long convertToLong(Object value) {
        if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L; // 默认值
    }
    @Override
    public List<Mail> getImportantMails(String email) {
        return mailRepository.findImportantMails(email);
    }

    @Override
    public List<Mail> getUnreadSummary(String email) {
        return mailRepository.findUnreadSummary(email);
    }

}
