package com.amrit.futsal.api;

import com.amrit.futsal.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(username = "owner@example.com", roles = "OWNER")
    void uploadFileRejectsOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ground.png",
                "image/png",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void uploadFileAllowsAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ground.png",
                "image/png",
                "content".getBytes()
        );
        when(fileStorageService.storeFile(any())).thenReturn("ground.png");

        mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "OWNER")
    void deleteFileRejectsOwner() throws Exception {
        mockMvc.perform(delete("/api/v1/files/test.png"))
                .andExpect(status().isForbidden());
    }
}
