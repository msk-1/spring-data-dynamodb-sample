package com.example.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDBConfig {
//    @Value("${amazon.dynamodb.endpoint}")
//    private String dynamoDbEndPointUrl;
//
//    @Value("${amazon.credential.profile}")
//    private String profile;

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonAWSCredentials().getAWSAccessKeyId(), amazonAWSCredentials().getAWSSecretKey())))
//                .withRegion(Regions.AP_NORTHEAST_1)
//                .build();

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "local"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonAWSCredentials().getAWSAccessKeyId(), amazonAWSCredentials().getAWSSecretKey())))
                .build();

        return new DynamoDBMapper(client, DynamoDBMapperConfig.DEFAULT);
    }

    @Bean
    public DynamoDbClient getDynamoDbClient() {
        Region region = Region.US_EAST_1;
        return DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000")).region(region)
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getDynamoDbClient())
                .build();
    }
    @Bean
    public AWSCredentials amazonAWSCredentials() {
        return new ProfileCredentialsProvider("default").getCredentials();
    }
}