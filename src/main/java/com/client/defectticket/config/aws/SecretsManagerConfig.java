package com.client.defectticket.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.net.URI;

/**
 * Secrets Manager configuration for secure credential storage.
 */
@Configuration
public class SecretsManagerConfig {

    @Value("${aws.secrets.endpoint:}")
    private String secretsEndpoint;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    @Profile("local | test")
    public SecretsManagerClient secretsManagerClientLocal(Region region) {
        var builder = SecretsManagerClient.builder()
                .region(region)
                .endpointOverride(secretsEndpoint != null && !secretsEndpoint.isEmpty() 
                    ? URI.create(secretsEndpoint) 
                    : null);
        
        // Use static credentials for LocalStack if provided
        if (accessKeyId != null && !accessKeyId.isEmpty() &&
            secretAccessKey != null && !secretAccessKey.isEmpty()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                )
            );
        }
        
        return builder.build();
    }

    @Bean
    @Profile("!local & !test")
    public SecretsManagerClient secretsManagerClientAws(Region region) {
        return SecretsManagerClient.builder()
                .region(region)
                .build();
    }
}
