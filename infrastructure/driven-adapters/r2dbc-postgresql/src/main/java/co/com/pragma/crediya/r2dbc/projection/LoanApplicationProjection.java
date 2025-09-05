package co.com.pragma.crediya.r2dbc.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanApplicationProjection(
        UUID id,
        BigDecimal amount,
        int term,
        String email,
        String type,
        BigDecimal interestRate,
        String status) {
}
