package com.example.demo.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface FileService  {
    Map<String, Object> saveBoardAttachment(MultipartFile mulFile, String uploadDir) throws IOException;

    String uploadFile(MultipartFile file) throws IOException;

    void deleteFile(String physicalFilePath);
}
