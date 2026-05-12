package com.example.demo.services;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
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
    
    public String getFileUrl(String folder, String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(folder + "/" + fileName)
                    .expiry(1, TimeUnit.HOURS)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL");
        }
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
