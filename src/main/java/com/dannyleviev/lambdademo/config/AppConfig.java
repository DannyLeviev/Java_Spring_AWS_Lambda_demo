package com.dannyleviev.lambdademo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    DynamoDbClient dynamoDbClient() {
        // Region + credentials come from the Lambda environment (default provider chains)
        return DynamoDbClient.builder()
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }

    @Bean
    DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    /** Injected instead of calling LocalDate.now() directly, so time is testable. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}