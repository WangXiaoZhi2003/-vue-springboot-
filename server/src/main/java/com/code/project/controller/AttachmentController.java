package com.code.project.controller;

import com.code.config.JwtUtil;
import com.code.project.service.AttachmentService;
import com.code.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 13:44
 */
@RestController
@RequestMapping("/api/attachment")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/upload")
    public ApiResponse<?> upload(@RequestParam("file") MultipartFile[] files, @RequestHeader("Authorization") String token) {
        List<String> paths = attachmentService.uploadFiles(jwtUtil.getEmail(token), files);
        return ApiResponse.success(paths);
        //return ResponseEntity.ok( Map.of("filePaths", paths));
    }

    @GetMapping("/download/**")
    public ResponseEntity<Resource> download(HttpServletRequest request) {
        // 获取请求中的完整路径
        String filePath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        // 去掉 "/download/" 前缀，获取相对路径
        String relativePath = filePath.replaceFirst("/api/attachment/download/", "");

        Resource file = attachmentService.download(relativePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }


}
