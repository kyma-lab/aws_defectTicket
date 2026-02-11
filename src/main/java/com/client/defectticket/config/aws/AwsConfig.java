package com.client.defectticket.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

/**
 * Base AWS configuration providing common beans for AWS SDK clients.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamodbEndpoint;

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public URI localstackEndpoint() {
        return dynamodbEndpoint != null && !dynamodbEndpoint.isEmpty() 
            ? URI.create(dynamodbEndpoint) 
            : null;
    }
}
