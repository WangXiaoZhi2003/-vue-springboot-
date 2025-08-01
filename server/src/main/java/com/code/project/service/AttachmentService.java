package com.code.project.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    List<String> uploadFiles(String email, MultipartFile[] files);
    Resource download(String filename);
}
