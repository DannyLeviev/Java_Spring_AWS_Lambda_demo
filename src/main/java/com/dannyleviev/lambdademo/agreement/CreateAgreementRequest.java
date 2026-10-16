package com.dannyleviev.lambdademo.agreement;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAgreementRequest(
        @NotBlank @Size(max = 40) String policyNumber,
        @NotBlank @Size(max = 120) String holderName,
        @NotNull ProductType productType,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 10, fraction = 2)
        BigDecimal monthlyPremium,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {
    @AssertTrue(message = "endDate must be after startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }
}