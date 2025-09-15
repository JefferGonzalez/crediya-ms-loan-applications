package co.com.pragma.crediya.usecase.loan.report;

import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.model.loan.report.CustomerApplication;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.model.loan.report.LoanApplicationsReport;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.user.User;
import co.com.pragma.crediya.model.user.gateways.UserPort;
import co.com.pragma.crediya.usecase.loan.utils.ApplicationCalculatorUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record ApplicationReportUseCase(ApplicationRepository applicationRepository,
                                       UserPort userPort,
                                       LoggerPort logger) {

    public Mono<LoanApplicationsReport> getLoanApplicationsReport(LoanApplicationFilter filter) {
        logger.info("Get loan applications report with filters: {}", filter);

        Mono<List<ApplicationReport>> reportsMono = filter.onlyPagination()
                ? applicationRepository.findApplicationsReport(filter.limit(), filter.page()).collectList()
                : applicationRepository.findApplicationsReport(filter).collectList();

        Mono<Long> countMono = filter.onlyPagination()
                ? applicationRepository.countLoanApplications()
                : applicationRepository.countLoanApplications(filter);

        return Mono.zip(reportsMono, countMono)
                .flatMap(tuple -> {
                    List<ApplicationReport> results = tuple.getT1();
                    long count = tuple.getT2();

                    if (results.isEmpty()) {
                        logger.info("No results found for the given filters parameters");

                        return Mono.just(new LoanApplicationsReport(List.of(), count));
                    }

                    Set<String> emails = results.stream()
                            .map(ApplicationReport::email)
                            .collect(Collectors.toSet());

                    logger.info("Collected emails from reports: {}", emails);

                    return userPort.getUsersByEmails(emails)
                            .map(users -> buildCustomerApplications(results, users))
                            .map(data -> new LoanApplicationsReport(data, count));
                })
                .doOnSuccess(report -> logger.info("Loan applications report generated successfully, total records={}", report.totalItems()))
                .doOnError(e -> logger.error("Failed to generate loan applications report", e));
    }

    private List<CustomerApplication> buildCustomerApplications(List<ApplicationReport> reports, List<User> users) {
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::email, u -> u));

        return reports.stream()
                .map(report -> {
                    User user = userMap.get(report.email());
                    BigDecimal baseSalary = user != null ? user.baseSalary() : BigDecimal.ZERO;

                    BigDecimal monthlyRate = ApplicationCalculatorUtils.annualToMonthlyRate(report.interestRate());
                    BigDecimal monthlyPayment = ApplicationCalculatorUtils.calculateMonthlyPayment(report.amount(), monthlyRate, report.term());

                    return new CustomerApplication(
                            report.id(),
                            report.email(),
                            baseSalary,
                            monthlyPayment,
                            report.type(),
                            report.amount(),
                            report.term(),
                            report.interestRate(),
                            report.status()
                    );
                })
                .toList();
    }

}
