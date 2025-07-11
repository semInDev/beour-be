package com.beour.global.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageUploader {

    @Value("${file.path}")
    private String filePath;

    @Value("${file.url}")
    private String fileUrl;

    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path savePath = Paths.get(filePath, fileName);
        Files.createDirectories(savePath.getParent());
        file.transferTo(savePath.toFile());

        return fileUrl + fileName;
    }

    // 다중 파일 업로드
//    public List<String> upload(List<MultipartFile> files) throws IOException {
//        if (files == null || files.isEmpty()) {
//            throw new IllegalArgumentException("파일 리스트가 비어있습니다.");
//        }
//
//        List<String> uploadedUrls = new ArrayList<>();
//        for (MultipartFile file : files) {
//            String uploadedUrl = upload(file);  // 기존 단일 업로드 재사용
//            uploadedUrls.add(uploadedUrl);
//        }
//
//        return uploadedUrls;
//    }

}
