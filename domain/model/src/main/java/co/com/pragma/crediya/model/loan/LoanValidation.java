package co.com.pragma.crediya.model.loan;

import java.util.List;
import java.util.UUID;

public record LoanValidation(
        UUID id,
        String status,
        List<PaymentDetail> paymentDetail) {
}