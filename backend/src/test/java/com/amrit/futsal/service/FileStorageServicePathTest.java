package com.amrit.futsal.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("kubernetes")
@TestPropertySource(properties = "file.upload-dir=/tmp/futsal-test-uploads")
class FileStorageServicePathTest {

    @Value("${file.upload-dir}")
    String configuredUploadDir;

    @Autowired
    FileStorageService fileStorageService;

    @Test
    void uploadDirIsConfigurable() {
        assertThat(configuredUploadDir).isEqualTo("/tmp/futsal-test-uploads");
        assertThat(fileStorageService).isNotNull();
    }
}
