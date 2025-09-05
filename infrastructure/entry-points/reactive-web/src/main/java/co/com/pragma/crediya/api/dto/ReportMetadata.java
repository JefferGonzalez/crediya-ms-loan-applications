package co.com.pragma.crediya.api.dto;

public record ReportMetadata(
        long totalItems,
        long totalPages,
        int page,
        int size) {
}
