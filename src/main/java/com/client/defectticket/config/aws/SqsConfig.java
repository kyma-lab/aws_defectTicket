package com.client.defectticket.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * SQS configuration for message queue handling.
 * Used for buffering batch ingestion requests before Step Functions execution.
 */
@Configuration
public class SqsConfig {

    @Value("${aws.sqs.endpoint:}")
    private String sqsEndpoint;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    @Profile("local | test")
    public SqsClient sqsClientLocal(Region region) {
        var builder = SqsClient.builder()
                .region(region)
                .endpointOverride(sqsEndpoint != null && !sqsEndpoint.isEmpty() 
                    ? URI.create(sqsEndpoint) 
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
    public SqsClient sqsClientAws(Region region) {
        return SqsClient.builder()
                .region(region)
                .build();
    }
}
