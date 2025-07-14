package com.beour.global.file;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ImageUploaderTest {

    @Autowired
    private ImageUploader imageUploader;

    @Value("${file.path}")
    private String filePath;
    @Value("${file.url}")
    private String fileUrl;

    @AfterEach
    void cleanUp() throws IOException {
        //test로 생성된 모든 파일 삭제
        Files.walk(Paths.get(filePath))
            .map(Path::toFile)
            .filter(File::isFile)
            .forEach(File::delete);
    }

//    @Test
//    @DisplayName("이미지 업로드 성공")
//    void success_upload() throws IOException {
//        // given
//        MockMultipartFile mockFile = new MockMultipartFile(
//            "image",
//            "sample.png",
//            "image/png",
//            "mock image content".getBytes()
//        );
//
//        // when
//        String url = imageUploader.upload(mockFile);
//
//        // then
//        assertThat(url).startsWith(fileUrl);
//
//        String savedFileName = url.replace(fileUrl, "");
//        File savedFile = new File(filePath + savedFileName);
//        assertThat(savedFile.exists()).isTrue();
//    }

}