package co.com.pragma.crediya.model.loan.report;

import java.util.List;

public record LoanApplicationsReport(
        List<CustomerApplication> data,
        long totalItems) {
}
