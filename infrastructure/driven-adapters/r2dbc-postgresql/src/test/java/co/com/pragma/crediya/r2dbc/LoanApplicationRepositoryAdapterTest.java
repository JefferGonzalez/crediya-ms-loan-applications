package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.crediya.r2dbc.mapper.LoanApplicationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
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
        verify(mapper, never()).toDomain(any());
    }
}
