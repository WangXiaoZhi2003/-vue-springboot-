package com.code.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    // 存储路径
    @Value("${mailsystem.profile}")
    private String uploadDir;

    @Override
    public List<String> uploadFiles(String email, MultipartFile[] files) {
        List<String> fileNames = new ArrayList<>();
        // 创建用户专属目录
        Path userDirPath = Paths.get(uploadDir,"upload", email);
        try {
            if (!Files.exists(userDirPath)) {
                Files.createDirectories(userDirPath);
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    Path filePath = userDirPath.resolve(originalFilename);
                    // 保存文件到指定位置
                    file.transferTo(filePath);
                    // 获取相对于 uploadDir 的完整相对路径
                    Path relativePath = Paths.get("upload",email, originalFilename);
                    fileNames.add(relativePath.toString().replace("\\", "/")); // 只返回相对路径
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        return fileNames;
    }

    @Override
    public Resource download(String filename) {
        try {
            // 假设所有文件都存储在 uploads 目录下
            Path filePath = Paths.get(uploadDir, filename);
            Resource resource = new FileSystemResource(filePath.toFile());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("文件不存在或不可读");
            }
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }
}
