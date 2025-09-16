package co.com.pragma.crediya.sqs.listener.dto;

import co.com.pragma.crediya.model.loan.LoanValidation;

public record LoanValidationResponse(
        String originalMessageId,
        LoanValidation validation) {
}