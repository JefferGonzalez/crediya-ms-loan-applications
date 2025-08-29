package co.com.pragma.crediya.model.loan;

import java.util.UUID;

public record Status(
        UUID id,
        String name,
        String description) {
}
