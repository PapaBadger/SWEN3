package org.swen.dms.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://minio:9000") // Ensure this matches your docker service name
                .credentials("minioadmin", "minioadmin")
                .build();
    }

    /**
     * This runs automatically on startup.
     * It checks if the "documents" bucket exists; if not, it creates it.
     */
    @Bean
    public CommandLineRunner initBucket(MinioClient minioClient) {
        return args -> {
            String bucketName = "documents";
            try {
                // 1. Check if bucket exists
                boolean found = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );

                if (!found) {
                    // 2. Create if missing
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucketName).build()
                    );
                    System.out.println("MinIO Bucket '" + bucketName + "' created successfully.");
                } else {
                    System.out.println("MinIO Bucket '" + bucketName + "' already exists.");
                }
            } catch (Exception e) {
                // Log the error but allow the app to keep running
                System.err.println("Error checking/creating MinIO bucket: " + e.getMessage());
            }
        };
    }
}