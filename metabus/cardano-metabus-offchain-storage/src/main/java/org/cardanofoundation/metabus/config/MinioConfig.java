package org.cardanofoundation.metabus.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * Docker network endpoint.
     */
    private String endpoint;

    /**
     * TCP/IP Port number.
     */
    private Integer port;

    /**
     * Public endpoint to expose pre-signed URL.
     */
    private String publicEndpoint;

    /**
     * Similar to user ID, Used to uniquely identify your account.
     */
    private String accessKey;

    /**
     * It's the password for your account.
     */
    private String secretKey;

    /**
     * If it is true, it uses https instead of http. The default value is true.
     */
    private boolean secure;

    /**
     * Default bucket.
     */
    private String bucketName;

    /**
     * The maximum size of the pictures.
     */
    private long imageSize;

    /**
     * Maximum size of other files.
     */
    private long fileSize;

    /**
     * Expire time (in minutes) to get object URL.
     */
    private Integer objectUrlExpiry;

    @Bean(value = "internalMinioClient")
    public MinioClient minioClient() {
        // Using docker network URL.
        return MinioClient.builder()
                .credentials(accessKey, secretKey)
                .endpoint(endpoint, port, secure)
                .build();
    }

    @Bean(value = "publicMinioClient")
    public MinioClient minioPublicClient() {
        MinioClient client;
        if (publicEndpoint.contains("localhost") || publicEndpoint.contains("127.0.0.1")) {
            // Local environment URL for debugging.
            client = MinioClient.builder()
                    .credentials(accessKey, secretKey)
                    .endpoint(publicEndpoint, port, secure)
                    .build();
        } else {
            // Using public domain URL.
            client = MinioClient.builder()
                    .credentials(accessKey, secretKey)
                    .endpoint(publicEndpoint)
                    .build();
        }
        return client;
    }
}
