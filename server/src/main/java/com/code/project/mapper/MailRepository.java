package com.code.project.mapper;

import com.code.project.domain.Mail;
import com.code.project.domain.MailStats;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MailRepository extends JpaRepository<Mail, Long> {
    // 添加查询已删除邮件的方法
    @Query("SELECT m FROM Mail m WHERE m.show = 1 AND (m.from = :email OR m.to = :email)")
    List<Mail> findDeletedMails(@Param("email") String email);
    // 修改：加入show条件
    List<Mail> findMailByToAndShowEquals(String to, Integer show);

    // 修改：加入show条件
    List<Mail> findMailByFromAndShowEquals(String from, Integer show);
    // 添加根据ID查询邮件的方法
    Optional<Mail> findById(Long id);
    // 修改：加入show条件
    Optional<Mail> findMailByIdAndToAndShowEquals(Long id, String email, Integer show);

    // 获取已发送邮件
    @Query("SELECT m FROM Mail m WHERE m.from = :from AND m.show = :show AND m.mailType = 'sent'")
    List<Mail> findSentMailsExcludingDrafts(@Param("from") String from, @Param("show") Integer show);
       // 修改：SQL中加入show=0条件
    @Query("SELECT m FROM Mail m WHERE m.from = :email AND (m.content LIKE %:keyword% OR m.subject LIKE %:keyword%) AND m.show = 0")
    List<Mail> searchByKeyword(@Param("email") String email, @Param("keyword")String keyword);
    // 新增垃圾邮件查询方法
    List<Mail> findByToAndStatusAndShowEquals(String to, String status, Integer show);
    // 新增：按ID和show状态查询
    Optional<Mail> findByIdAndShowEquals(Long id, Integer show);
    // 新增草稿邮件查询方法
    List<Mail> findMailByFromAndStatusAndShowEquals(String from, String status, Integer show);
    // 获取收件箱邮件（接收到的邮件）
    @Query("SELECT m FROM Mail m WHERE m.to = :to AND m.show = :show AND m.mailType = 'inbox' AND m.status = 'normal'")
    List<Mail> findInboxMails(@Param("to") String to, @Param("show") Integer show);
    // 统计邮件数据
    @Query("SELECT " +
            "SUM(CASE WHEN m.to = :email AND m.read = 0 AND m.mailType = 'inbox' AND m.show = 0 THEN 1 ELSE 0 END) as unreadCount, " +
            "SUM(CASE WHEN (m.to = :email) AND m.starred = true AND m.show = 0 THEN 1 ELSE 0 END) as starredCount, " +
            "SUM(CASE WHEN m.from = :email AND m.mailType = 'sent' AND m.show = 0 THEN 1 ELSE 0 END) as sentCount, " +
            "SUM(CASE WHEN m.from = :email AND m.status = 'draft' AND m.show = 0 THEN 1 ELSE 0 END) as draftCount " +
            "FROM Mail m")
    MailStats getMailStats(@Param("email") String email);


    // 查询重要邮件
    @Query("SELECT m FROM Mail m WHERE m.starred = true AND m.show = 0 AND (m.to = :email)")
    List<Mail> findImportantMails(@Param("email") String email);

    // 查询未读邮件摘要
    @Query("SELECT m FROM Mail m WHERE m.read = 0 AND m.mailType = 'inbox' AND m.show = 0 AND m.to = :email")
    List<Mail> findUnreadSummary(@Param("email") String email);


}
