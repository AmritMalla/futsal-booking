package com.amrit.futsal.service;

import com.amrit.futsal.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;

    private Path fileStorageLocation;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory for file uploads", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        validateStoredFileName(fileName);
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    public Path getFilePath(String fileName) {
        validateStoredFileName(fileName);
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFileName.contains("..")) {
            throw new BadRequestException("Invalid file path sequence in filename");
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        List<String> allowedExtList = Arrays.asList(allowedExtensions.split(","));
        if (!allowedExtList.contains(fileExtension)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedExtensions);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid content type. Only images are allowed.");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void validateStoredFileName(String fileName) {
        String cleanedFileName = StringUtils.cleanPath(fileName);
        if (!StringUtils.hasText(cleanedFileName) || cleanedFileName.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
    }
}
