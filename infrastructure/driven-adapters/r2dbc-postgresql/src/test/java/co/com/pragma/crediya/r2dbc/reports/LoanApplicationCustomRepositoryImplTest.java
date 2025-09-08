package co.com.pragma.crediya.r2dbc.reports;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationCustomRepositoryImplTest {

    @Mock
    private DatabaseClient client;

    @Mock
    DatabaseClient.GenericExecuteSpec spec;

    @Mock
    RowsFetchSpec<LoanApplicationProjection> fetchSpec;

    @Mock
    RowsFetchSpec<Long> fetchSpecCount;

    @InjectMocks
    LoanApplicationCustomRepositoryImpl repository;

    private LoanApplicationFilter filter;

    private LoanApplicationProjection loanApplicationProjection;

    @BeforeEach
    void setUp() {
        filter = new LoanApplicationFilter(
                Set.of("APPROVED"),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(5000),
                6,
                12,
                "jhondoe@example.com",
                DomainConstants.MICROCREDIT,
                5,
                1
        );

        loanApplicationProjection = new LoanApplicationProjection(UUID.randomUUID(), BigDecimal.valueOf(1000), 12, "jhondoe@example.com", DomainConstants.MICROCREDIT, BigDecimal.valueOf(5), DomainConstants.DEFAULT_PENDING_STATUS);

        when(client.sql(anyString())).thenReturn(spec);

        when(spec.bind(anyString(), any())).thenReturn(spec);
    }

    @Test
    void findLoanApplications_shouldReturnFlux() {
        when(spec.map(any(BiFunction.class))).thenReturn(fetchSpec);

        when(fetchSpec.all()).thenReturn(Flux.just(loanApplicationProjection));

        StepVerifier.create(repository.findLoanApplications(filter))
                .assertNext(projection -> assertThat(projection.status()).isEqualTo(DomainConstants.DEFAULT_PENDING_STATUS))
                .verifyComplete();

        verify(spec, atLeastOnce()).bind(anyString(), any());
    }

    @Test
    void countLoanApplications_shouldReturnMono() {
        long count = 5L;

        when(spec.map(any(BiFunction.class))).thenReturn(fetchSpecCount);

        when(fetchSpecCount.one()).thenReturn(Mono.just(count));

        Mono<Long> result = repository.countLoanApplications(filter);

        StepVerifier.create(result)
                .assertNext(c -> assertThat(c).isEqualTo(count))
                .verifyComplete();

        verify(spec, atLeastOnce()).bind(anyString(), any());
    }

}
