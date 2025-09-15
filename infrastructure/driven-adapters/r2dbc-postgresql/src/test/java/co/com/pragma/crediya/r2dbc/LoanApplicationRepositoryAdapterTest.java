package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.ActiveApplication;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.crediya.r2dbc.mapper.LoanApplicationMapper;
import co.com.pragma.crediya.r2dbc.projection.LoanAmortizationProjection;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationRepositoryAdapterTest {

    @Mock
    private LoanApplicationReactiveRepository reactiveRepository;

    @Mock
    private LoanApplicationMapper mapper;

    @InjectMocks
    private LoanApplicationRepositoryAdapter adapter;

    private Application application;

    private LoanApplicationEntity loanApplicationEntity;

    private LoanApplicationFilter filter;

    private LoanApplicationProjection loanApplicationProjection;

    private ApplicationReport report;

    @BeforeEach
    void setUp() {
        application = new Application(
                UUID.randomUUID(),
                BigDecimal.valueOf(5000000),
                12,
                "1234567890",
                "jhondoe@example.com",
                null,
                null
        );

        loanApplicationEntity = new LoanApplicationEntity(application.id(), application.amount(), application.term(), application.identificationNumber(), application.email(), null, null);

        report = new ApplicationReport(
                UUID.randomUUID(),
                BigDecimal.valueOf(5000000),
                12,
                "1234567890",
                "jhondoe@example.com",
                BigDecimal.valueOf(5), DomainConstants.DEFAULT_PENDING_STATUS
        );

        loanApplicationProjection = new LoanApplicationProjection(report.id(), report.amount(), report.term(), report.email(), report.type(), report.interestRate(), report.status());

        filter = new LoanApplicationFilter(
                Set.of("APPROVED"),
                BigDecimal.valueOf(4000000),
                BigDecimal.valueOf(8000000),
                6,
                12,
                "jhondoe@example.com",
                DomainConstants.MICROCREDIT,
                5,
                1
        );
    }

    @Test
    void findById_ShouldReturnApplication_WhenEntityExists() {
        when(reactiveRepository.findById(application.id()))
                .thenReturn(Mono.just(loanApplicationEntity));

        when(mapper.toDomain(loanApplicationEntity)).thenReturn(application);

        StepVerifier.create(adapter.findById(application.id()))
                .expectNext(application)
                .verifyComplete();

        verify(reactiveRepository).findById(application.id());

        verify(mapper).toDomain(loanApplicationEntity);
    }

    @Test
    void saveApplicationSuccessfully() {
        when(mapper.toEntity(application)).thenReturn(loanApplicationEntity);

        when(reactiveRepository.save(loanApplicationEntity)).thenReturn(Mono.just(loanApplicationEntity));

        when(mapper.toDomain(loanApplicationEntity)).thenReturn(application);

        StepVerifier.create(adapter.save(application))
                .expectNext(application)
                .verifyComplete();

        verify(mapper).toEntity(application);
        verify(reactiveRepository).save(loanApplicationEntity);
        verify(mapper).toDomain(loanApplicationEntity);
    }

    @Test
    void saveApplicationPropagatesError() {
        when(mapper.toEntity(application)).thenReturn(loanApplicationEntity);

        when(reactiveRepository.save(loanApplicationEntity)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(application))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("DB error"))
                .verify();

        verify(mapper).toEntity(application);

        verify(reactiveRepository).save(loanApplicationEntity);

        verify(mapper, never()).toDomain(any(LoanApplicationEntity.class));
    }

    @Test
    void findApplicationsReportByLimitAndPage_shouldReturnFlux() {
        int limit = 10;
        int page = 2;
        int offset = (page - 1) * limit;

        when(reactiveRepository.queryLoanApplications(limit, offset)).thenReturn(Flux.just(loanApplicationProjection));

        when(mapper.toDomain(loanApplicationProjection)).thenReturn(report);

        StepVerifier.create(adapter.findApplicationsReport(limit, page))
                .expectNext(report)
                .verifyComplete();

        verify(reactiveRepository).queryLoanApplications(limit, offset);

        verify(mapper).toDomain(loanApplicationProjection);
    }

    @Test
    void findApplicationsReportWithFilter_shouldReturnFlux() {
        when(reactiveRepository.findLoanApplications(filter)).thenReturn(Flux.just(loanApplicationProjection));

        when(mapper.toDomain(loanApplicationProjection)).thenReturn(report);

        StepVerifier.create(adapter.findApplicationsReport(filter))
                .expectNext(report)
                .verifyComplete();

        verify(reactiveRepository).findLoanApplications(filter);

        verify(mapper).toDomain(loanApplicationProjection);
    }

    @Test
    void countLoanApplications_shouldReturnMono() {
        when(reactiveRepository.countLoanApplications()).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.countLoanApplications())
                .expectNext(5L)
                .verifyComplete();

        verify(reactiveRepository).countLoanApplications();
    }

    @Test
    void countLoanApplicationsWithFilter_shouldReturnMono() {
        when(reactiveRepository.countLoanApplications(filter)).thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.countLoanApplications(filter))
                .expectNext(3L)
                .verifyComplete();

        verify(reactiveRepository).countLoanApplications(filter);
    }

    @Test
    void findActiveLoansByIdentificationNumber_ShouldReturnActiveApplications_WhenEntitiesExist() {
        ActiveApplication activeApplication = new ActiveApplication(BigDecimal.valueOf(1000000), 12, BigDecimal.valueOf(20.5));

        LoanAmortizationProjection projection = new LoanAmortizationProjection(activeApplication.amount(), activeApplication.term(), activeApplication.interestRate());

        when(reactiveRepository.queryActiveLoansByIdentificationNumber(application.identificationNumber()))
                .thenReturn(Flux.just(projection));

        when(mapper.toDomain(projection)).thenReturn(activeApplication);

        StepVerifier.create(adapter.findActiveLoansByIdentificationNumber(application.identificationNumber()))
                .expectNext(activeApplication)
                .verifyComplete();

        verify(reactiveRepository).queryActiveLoansByIdentificationNumber(application.identificationNumber());

        verify(mapper).toDomain(projection);
    }

}
