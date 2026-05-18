package com.example.demo.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.public-url}")
    private String publicUrl;

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String objectName = folder + "/" + fileName;
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        return fileName;
    }

    public String getPublicUrl(String folder, String fileName) {
        if (fileName == null) return null;
        return publicUrl + "/" + bucketName + "/" + folder + "/" + fileName;
    }

    public void deleteFile(String folder, String fileName) throws Exception {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(folder + "/" + fileName)
                .build()
        );
    }
}
