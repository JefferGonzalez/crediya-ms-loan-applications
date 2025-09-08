package co.com.pragma.crediya.usecase.loan.report;

import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.model.loan.report.CustomerApplication;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.user.User;
import co.com.pragma.crediya.model.user.gateways.UserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationReportUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserPort userPort;

    @Mock
    private LoggerPort logger;

    private ApplicationReportUseCase useCase;

    private LoanApplicationFilter paginationFilter;

    private LoanApplicationFilter filter;

    private ApplicationReport report1;

    private ApplicationReport report2;

    private User user;

    private final String email = "jhon.doe@example.com";

    @BeforeEach
    void setUp() {
        paginationFilter = new LoanApplicationFilter(null, null, null, null, null, null, null, 10, 0);

        filter = new LoanApplicationFilter(Set.of("APPROVED"), BigDecimal.valueOf(1000), BigDecimal.valueOf(5000), 6, 24, email, "MICROCREDIT", 10, 0);

        report1 = new ApplicationReport(UUID.randomUUID(), BigDecimal.valueOf(2000), 12, email, "MICROCREDIT", BigDecimal.valueOf(2), "APPROVED");

        report2 = new ApplicationReport(UUID.randomUUID(), BigDecimal.valueOf(3000), 12, email, "MICROCREDIT", BigDecimal.valueOf(2), "APPROVED");

        user = new User("12345", email, BigDecimal.valueOf(10000));

        useCase = new ApplicationReportUseCase(applicationRepository, userPort, logger);
    }

    @Test
    @DisplayName("getLoanApplicationsReport() with pagination returns correct data")
    void getLoanApplicationsReport_withPagination_shouldReturnReport() {
        when(applicationRepository.findApplicationsReport(paginationFilter.limit(), paginationFilter.page()))
                .thenReturn(Flux.just(report1));

        when(applicationRepository.countLoanApplications()).thenReturn(Mono.just(1L));

        when(userPort.getUsersByEmails(Set.of(email))).thenReturn(Mono.just(List.of(user)));

        StepVerifier.create(useCase.getLoanApplicationsReport(paginationFilter))
                .assertNext(result -> {
                    assertThat(result.totalItems()).isEqualTo(1);
                    assertThat(result.data()).hasSize(1);

                    CustomerApplication app = result.data().getFirst();
                    assertThat(app.email()).isEqualTo(email);
                    assertThat(app.baseSalary()).isEqualTo(BigDecimal.valueOf(10000));
                    assertThat(app.monthlyPayment()).isGreaterThan(BigDecimal.ZERO);
                }).verifyComplete();
    }

    @Test
    @DisplayName("getLoanApplicationsReport() with filters returns correct data")
    void getLoanApplicationsReport_withFilters_shouldReturnReport() {
        when(applicationRepository.findApplicationsReport(filter)).thenReturn(Flux.just(report1));

        when(applicationRepository.countLoanApplications(filter)).thenReturn(Mono.just(1L));

        when(userPort.getUsersByEmails(Set.of(email))).thenReturn(Mono.just(List.of(user)));

        StepVerifier.create(useCase.getLoanApplicationsReport(filter))
                .assertNext(result -> {
                    assertThat(result.totalItems()).isEqualTo(1);
                    assertThat(result.data()).hasSize(1);

                    CustomerApplication app = result.data().getFirst();
                    assertThat(app.email()).isEqualTo(email);
                    assertThat(app.baseSalary()).isEqualTo(BigDecimal.valueOf(10000));
                    assertThat(app.status()).isEqualTo("APPROVED");
                }).verifyComplete();
    }

    @Test
    @DisplayName("getLoanApplicationsReport() with no results returns empty list")
    void getLoanApplicationsReport_whenNoResults_shouldReturnEmptyList() {
        when(applicationRepository.findApplicationsReport(any(LoanApplicationFilter.class)))
                .thenReturn(Flux.empty());

        when(applicationRepository.countLoanApplications(any(LoanApplicationFilter.class)))
                .thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.getLoanApplicationsReport(filter))
                .assertNext(result -> {
                    assertThat(result.totalItems()).isZero();
                    assertThat(result.data()).isEmpty();
                }).verifyComplete();
    }

    @Test
    @DisplayName("getLoanApplicationsReport() returns separate entries for same email")
    void getLoanApplicationsReport_withMultipleReportsForSameEmail_shouldAggregateDebt() {
        when(applicationRepository.findApplicationsReport(filter)).thenReturn(Flux.just(report1, report2));

        when(applicationRepository.countLoanApplications(filter)).thenReturn(Mono.just(2L));

        when(userPort.getUsersByEmails(Set.of(email))).thenReturn(Mono.just(List.of(user)));

        StepVerifier.create(useCase.getLoanApplicationsReport(filter))
                .assertNext(report -> {
                    assertThat(report.totalItems()).isEqualTo(2);
                    assertThat(report.data()).hasSize(2);

                    CustomerApplication item = report.data().getFirst();
                    assertThat(item.monthlyPayment()).isGreaterThan(BigDecimal.ZERO);
                }).verifyComplete();
    }

    @Test
    @DisplayName("getLoanApplicationsReport() handles repository errors correctly")
    void getLoanApplicationsReport_shouldHandleRepositoryErrors() {
        when(applicationRepository.findApplicationsReport(any(LoanApplicationFilter.class)))
                .thenReturn(Flux.error(new RuntimeException("DB error")));

        when(applicationRepository.countLoanApplications(any(LoanApplicationFilter.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.getLoanApplicationsReport(filter))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("DB error"))
                .verify();
    }

    @Test
    @DisplayName("getLoanApplicationsReport() sets baseSalary to zero when userPort returns empty")
    void getLoanApplicationsReport_shouldSetZeroBaseSalaryWhenUserNotFound() {
        ApplicationReport report = new ApplicationReport(
                UUID.randomUUID(),
                BigDecimal.valueOf(10000),
                12,
                null,
                "Personal",
                BigDecimal.valueOf(5),
                "APPROVED"
        );

        when(applicationRepository.findApplicationsReport(any(LoanApplicationFilter.class)))
                .thenReturn(Flux.just(report));

        when(applicationRepository.countLoanApplications(any(LoanApplicationFilter.class)))
                .thenReturn(Mono.just(1L));

        when(userPort.getUsersByEmails(anySet())).thenReturn(Mono.just(List.of()));

        StepVerifier.create(useCase.getLoanApplicationsReport(filter))
                .assertNext(result -> {
                    CustomerApplication customer = result.data().getFirst();

                    assertThat(customer.baseSalary()).isEqualTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

}
