package com.amrit.futsal.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "file.upload-dir=target/test-uploads")
class FileEndpointIntegrationTest {

    private static final Path TEST_UPLOAD_DIR = Paths.get("target", "test-uploads").toAbsolutePath().normalize();

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanUploads() throws IOException {
        if (!Files.exists(TEST_UPLOAD_DIR)) {
            return;
        }

        try (var paths = Files.walk(TEST_UPLOAD_DIR)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }

    @Test
    @WithMockUser(username = "admin.file@example.com", roles = "ADMIN")
    void adminCanUploadAndDownloadImageFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ground.png",
                "image/png",
                "png-content".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").isNotEmpty())
                .andExpect(jsonPath("$.fileType").value("image/png"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String fileName = response.replaceAll(".*\"fileName\":\"([^\"]+)\".*", "$1");
        Path storedFile = TEST_UPLOAD_DIR.resolve(fileName);
        assertThat(Files.exists(storedFile)).isTrue();

        mockMvc.perform(get("/api/v1/files/%s".formatted(fileName)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes("png-content".getBytes()));
    }

    @Test
    @WithMockUser(username = "admin.file@example.com", roles = "ADMIN")
    void uploadRejectsInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "not-an-image".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File type not allowed. Allowed types: jpg,jpeg,png,gif,webp"));
    }

    @Test
    @WithMockUser(username = "owner.file@example.com", roles = "OWNER")
    void ownerCannotDeleteUploadedFile() throws Exception {
        Path existingFile = Files.createDirectories(TEST_UPLOAD_DIR).resolve("existing.png");
        Files.writeString(existingFile, "content");

        mockMvc.perform(delete("/api/v1/files/existing.png"))
                .andExpect(status().isForbidden());

        assertThat(Files.exists(existingFile)).isTrue();
    }

    @Test
    @WithMockUser(username = "admin.file@example.com", roles = "ADMIN")
    void adminCanDeleteUploadedFile() throws Exception {
        Path existingFile = Files.createDirectories(TEST_UPLOAD_DIR).resolve("existing.png");
        Files.writeString(existingFile, "content");

        mockMvc.perform(delete("/api/v1/files/existing.png"))
                .andExpect(status().isNoContent());

        assertThat(Files.exists(existingFile)).isFalse();
    }
}
