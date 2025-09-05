package co.com.pragma.crediya.usecase.loan.report;

import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.report.CustomerApplication;
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

        Mono<List<ApplicationReport>> reportsMono;
        Mono<Long> countMono;

        if (filter.onlyPagination()) {
            reportsMono = applicationRepository
                    .findApplicationsReport(filter.limit(), filter.page())
                    .collectList();
            countMono = applicationRepository.countLoanApplications();
        } else {
            reportsMono = applicationRepository
                    .findApplicationsReport(filter)
                    .collectList();
            countMono = applicationRepository.countLoanApplications(filter);
        }

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
                            .map(users -> {
                                Map<String, User> userMap = users.stream()
                                        .collect(Collectors.toMap(User::email, u -> u));

                                Map<String, BigDecimal> debtsByEmail = results.stream()
                                        .collect(Collectors.groupingBy(
                                                ApplicationReport::email,
                                                Collectors.reducing(
                                                        BigDecimal.ZERO,
                                                        r -> ApplicationCalculatorUtils.calculateMonthlyPayment(
                                                                r.amount(),
                                                                r.interestRate(),
                                                                r.term()
                                                        ),
                                                        BigDecimal::add
                                                )
                                        ));

                                return results.stream()
                                        .map(r -> {
                                            User user = userMap.get(r.email());
                                            BigDecimal baseSalary = user != null ? user.baseSalary() : BigDecimal.ZERO;
                                            BigDecimal totalMonthlyDebt = debtsByEmail.getOrDefault(r.email(), BigDecimal.ZERO);

                                            logger.info("Preparing CustomerApplication for email: {}", r.email());

                                            return new CustomerApplication(
                                                    r.email(),
                                                    baseSalary,
                                                    totalMonthlyDebt,
                                                    r.type(),
                                                    r.amount(),
                                                    r.term(),
                                                    r.interestRate(),
                                                    r.status()
                                            );
                                        })
                                        .toList();
                            })
                            .map(data -> new LoanApplicationsReport(data, count));
                });
    }

}
