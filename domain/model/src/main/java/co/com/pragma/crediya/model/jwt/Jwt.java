package co.com.pragma.crediya.model.jwt;

import java.math.BigDecimal;
import java.util.List;

public record Jwt(String subject, List<String> roles, String identificationNumber, BigDecimal baseSalary) {
}
