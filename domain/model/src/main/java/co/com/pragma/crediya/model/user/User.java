package co.com.pragma.crediya.model.user;

import java.math.BigDecimal;

public record User(
        String identificationNumber,
        String email,
        BigDecimal baseSalary) {
}