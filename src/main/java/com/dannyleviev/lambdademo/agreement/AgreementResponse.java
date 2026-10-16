package com.dannyleviev.lambdademo.agreement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AgreementResponse(
        String id,
        String policyNumber,
        String holderName,
        ProductType productType,
        BigDecimal monthlyPremium,
        LocalDate startDate,
        LocalDate endDate,
        AgreementStatus status,
        Instant createdAt
) {
    static AgreementResponse from(AgreementItem item) {
        return new AgreementResponse(
                item.getId(),
                item.getPolicyNumber(),
                item.getHolderName(),
                ProductType.valueOf(item.getProductType()),
                item.getMonthlyPremium(),
                item.getStartDate(),
                item.getEndDate(),
                AgreementStatus.valueOf(item.getStatus()),
                item.getCreatedAt());
    }
}