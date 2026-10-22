package com.dannyleviev.lambdademo.agreement;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Persistence model. Mutable + no-arg constructor because the Enhanced Client's
 * bean schema requires getters/setters. API DTOs stay immutable records.
 */
@DynamoDbBean
@Data
@NoArgsConstructor
public class AgreementItem {


    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String id;

    private String policyNumber;
    private String holderName;
    private String productType;
    private BigDecimal monthlyPremium;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Instant createdAt;
}