package com.cetiti.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class MinioClientConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.accessKey}")
    private String accessKey;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }
}