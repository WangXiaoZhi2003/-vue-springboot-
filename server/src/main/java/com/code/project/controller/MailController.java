package com.code.project.controller;

import com.code.config.JwtUtil;
import com.code.project.domain.Mail;
import com.code.project.domain.MailDTO;
import com.code.project.service.MailService;
import com.code.web.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:18
 */
@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @Autowired
    private JwtUtil jwtUtil;

    //发送邮件
    @PostMapping("/send")
    public ApiResponse<?> sendMail(@RequestBody MailDTO mailDTO, @RequestHeader("Authorization") String token) {
        mailService.sendMail(jwtUtil.getEmail(token), mailDTO);
        return ApiResponse.success("邮件发送成功");
        //return ResponseEntity.ok(Map.of("code", 200, "message", "邮件发送成功"));
    }

    //获取收件箱邮件
    @GetMapping("/inbox")
    public ApiResponse<?> getInbox(@RequestHeader("Authorization") String token) {
        List<Mail> mails = mailService.getInbox(jwtUtil.getEmail(token));
        return ApiResponse.success(mails);
        //return ResponseEntity.ok(Map.of("code", 200, "data", mails));
    }
    // 新增草稿保存接口
    @PostMapping("/senddraft")
    public ApiResponse<?> saveDraft(
            @RequestBody MailDTO mailDTO,
            @RequestHeader("Authorization") String token) {
        mailService.saveDraft(jwtUtil.getEmail(token), mailDTO);
        return ApiResponse.success("草稿保存成功");
    }
    //获取已发送邮件
    @GetMapping("/sent")
    public ApiResponse<?> getSent(@RequestHeader("Authorization") String token) {
        List<Mail> mails = mailService.getSent(jwtUtil.getEmail(token));
        return ApiResponse.success(mails);
        //return ResponseEntity.ok(Map.of("code", 200, "data", mails));
    }
    // 获取垃圾邮件
    @GetMapping("/spam")
    public ApiResponse<?> getSpam(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        List<Mail> spamMails = mailService.getSpam(email);
        return ApiResponse.success(spamMails);
    }

    // 新增获取草稿邮件接口
    @GetMapping("/drafts")
    public ApiResponse<?> getDrafts(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        List<Mail> drafts = mailService.getDrafts(email);
        return ApiResponse.success(drafts);
    }

    //搜索邮件
    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam String keyword, @RequestHeader("Authorization") String token) {
        List<Mail> mails = mailService.search(jwtUtil.getEmail(token), keyword);
        return ApiResponse.success(mails);
        //return ResponseEntity.ok(Map.of("code", 200, "data", mails));
    }

    @PostMapping("/mark-read/{mailId}")
    public ApiResponse<?> markAsRead(@PathVariable Long mailId, @RequestHeader("Authorization") String token) {
        mailService.markAsRead(jwtUtil.getEmail(token), mailId);
        return ApiResponse.success("已标记为已读");
       // return ResponseEntity.ok(Map.of("code", 200,"message", "已标记为已读"));
    }

    /**
     * 获取邮件详情
     * @param id 邮件ID
     * @param token 认证令牌
     * @return 邮件详情响应
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<?> getMailDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String email = jwtUtil.getEmail(token);

        // 直接调用服务层获取邮件
        Mail mail = mailService.getMailDetail(email, id);

        // 转换附件路径为数组
        List<String> attachmentList = mail.getAttachmentPaths() == null ?
                Collections.emptyList() :
                Arrays.asList(mail.getAttachmentPaths().split(","));

        // 构建响应对象
        Map<String, Object> response = new HashMap<>();
        response.put("id", mail.getId());
        response.put("from", mail.getFrom());
        response.put("to", mail.getTo());
        response.put("subject", mail.getSubject());
        response.put("content", mail.getContent());
        response.put("timestamp", mail.getTimestamp());
        response.put("attachmentPaths", attachmentList);
        response.put("starred", mail.getStarred());


        return ApiResponse.success(response);
    }

    // 将逗号分隔的附件路径转换为数组
    private List<String> convertAttachmentPaths(String paths) {
        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(paths.split(","));
    }
    // 添加获取已删除邮件接口
    @GetMapping("/deleted")
    public ApiResponse<?> getDeleted(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        List<Mail> deletedMails = mailService.getDeleted(email);
        return ApiResponse.success(deletedMails);
    }
    // 删除邮件接口
    @DeleteMapping("/delete/{mailId}")
    public ApiResponse<?> deleteMail(
            @PathVariable Long mailId,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        mailService.deleteMail(email, mailId);
        return ApiResponse.success("邮件删除成功");
    }

    /**
     * 切换邮件星标状态
     * @param mailId 邮件ID
     * @param token 用户令牌
     * @return 操作结果
     */
    @PostMapping("/toggle-star/{mailId}")
    public ApiResponse<?> toggleStar(
            @PathVariable Long mailId,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        mailService.toggleStar(email, mailId);
        return ApiResponse.success("星标状态已更新");
    }

    // 获取邮件统计数据
    @GetMapping("/stats")
    public ApiResponse<?> getMailStats(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        return ApiResponse.success(mailService.getMailStats(email));
    }

    // 获取重要邮件
    @GetMapping("/important")
    public ApiResponse<?> getImportantMails(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        List<Mail> mails = mailService.getImportantMails(email);
        return ApiResponse.success(mails.stream().map(mail -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mail.getId());
            map.put("sender", mail.getFrom());
            map.put("subject", mail.getSubject());
            map.put("preview", mail.getContent().substring(0, Math.min(50, mail.getContent().length())) + "...");
            map.put("timestamp", mail.getTimestamp());
            return map;
        }).collect(Collectors.toList()));
    }

    // 获取未读邮件摘要
    @GetMapping("/unread")
    public ApiResponse<?> getUnreadMails(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token);
        List<Mail> mails = mailService.getUnreadSummary(email);
        return ApiResponse.success(mails.stream().map(mail -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mail.getId());
            map.put("sender", mail.getFrom());
            map.put("subject", mail.getSubject());
            map.put("timestamp", mail.getTimestamp());
            return map;
        }).collect(Collectors.toList()));
    }


}
