package com.client.defectticket.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.net.URI;

/**
 * Step Functions configuration for workflow orchestration.
 * Handles pause/resume for HITL approval gates.
 */
@Configuration
public class StepFunctionsConfig {

    @Value("${aws.stepfunctions.endpoint:}")
    private String sfnEndpoint;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    @Profile("local | test")
    public SfnClient sfnClientLocal(Region region) {
        var builder = SfnClient.builder()
                .region(region)
                .endpointOverride(sfnEndpoint != null && !sfnEndpoint.isEmpty() 
                    ? URI.create(sfnEndpoint) 
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
    public SfnClient sfnClientAws(Region region) {
        return SfnClient.builder()
                .region(region)
                .build();
    }
}
