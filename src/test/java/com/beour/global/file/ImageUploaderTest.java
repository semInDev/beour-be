package com.beour.global.file;

import com.beour.global.exception.exceptionType.ImageFileInvalidException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageUploaderTest {

    @Autowired
    private ImageUploader imageUploader;

    @Value("${file.path}")
    private String filePath;

    @Value("${file.url}")
    private String fileUrl;

    @BeforeAll
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(filePath));
    }

    @AfterEach
    void cleanUp() throws IOException {
        Path uploadDir = Paths.get(filePath);
        if (Files.exists(uploadDir)) {
            Files.walk(uploadDir)
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("이미지 업로드 - 성공")
    void success_upload() throws IOException {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                "sample.png",
                "image/png",
                "mock image content".getBytes()
        );

        // when
        String url = imageUploader.upload(mockFile);

        // then
        assertThat(url).startsWith(fileUrl);

        String savedFileName = url.replace(fileUrl, "");
        File savedFile = new File(filePath + savedFileName);
        assertThat(savedFile.exists()).isTrue();
    }

    @Test
    @DisplayName("이미지 업로드 - 빈 파일")
    void upload_emptyFile() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThrows(ImageFileInvalidException.class, () -> imageUploader.upload(emptyFile));
    }
}
