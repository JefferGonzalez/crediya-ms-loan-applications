package co.com.pragma.crediya.model.notification;

import co.com.pragma.crediya.model.loan.PaymentDetail;

import java.math.BigDecimal;
import java.util.List;

public record LoanApproval(
        String loanType,
        BigDecimal amount,
        int term,
        BigDecimal interestRate,
        BigDecimal monthlyPayment,
        List<PaymentDetail> paymentDetail) {
}
