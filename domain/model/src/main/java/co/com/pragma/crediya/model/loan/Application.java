package co.com.pragma.crediya.model.loan;

import java.math.BigDecimal;
import java.util.UUID;

public record Application(
        UUID id,
        BigDecimal amount,
        int term,
        String identificationNumber,
        String email,
        Type type,
        Status status) {
}
