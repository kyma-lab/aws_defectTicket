package com.client.defectticket.config.aws;

import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.WorkflowState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * DynamoDB configuration with Enhanced Client for simplified table operations.
 * Supports LocalStack for local development and testing.
 */
@Configuration
public class DynamoDbConfig {

    @Value("${aws.dynamodb.table-prefix}")
    private String tablePrefix;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    @Profile("local | test")
    public DynamoDbClient dynamoDbClientLocal(Region region, URI localstackEndpoint) {
        var builder = DynamoDbClient.builder()
                .region(region)
                .endpointOverride(localstackEndpoint);
        
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
    public DynamoDbClient dynamoDbClientAws(Region region) {
        return DynamoDbClient.builder()
                .region(region)
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<DefectTicket> defectTicketTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tablePrefix + "-tickets", TableSchema.fromBean(DefectTicket.class));
    }

    @Bean
    public DynamoDbTable<ApprovalRequest> approvalRequestTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tablePrefix + "-approvals", TableSchema.fromBean(ApprovalRequest.class));
    }

    @Bean
    public DynamoDbTable<WorkflowState> workflowStateTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tablePrefix + "-workflow-states", TableSchema.fromBean(WorkflowState.class));
    }
}
