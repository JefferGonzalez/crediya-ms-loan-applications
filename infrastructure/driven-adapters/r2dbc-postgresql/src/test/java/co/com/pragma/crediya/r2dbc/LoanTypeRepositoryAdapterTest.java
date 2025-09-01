package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.r2dbc.entity.LoanTypeEntity;
import co.com.pragma.crediya.r2dbc.mapper.LoanTypeMapper;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanTypeRepositoryAdapterTest {

    @Mock
    private LoanTypeReactiveRepository loanTypeReactiveRepository;

    @Mock
    private LoanTypeMapper loanTypeMapper;

    @InjectMocks
    private LoanTypeRepositoryAdapter loanTypeRepositoryAdapter;

    private LoanTypeEntity loanTypeEntity;

    private Type type;

    @BeforeEach
    void setUp() {
        type = new Type(UUID.randomUUID(), DomainConstants.MICROCREDIT, BigDecimal.valueOf(300000), BigDecimal.valueOf(50000000), BigDecimal.valueOf(25.00), true);
        loanTypeEntity = new LoanTypeEntity(type.id(), type.name(), type.minimumAmount(), type.maximumAmount(), type.interestRate(), type.automaticValidation());
    }

    @Test
    void findByName_ShouldReturnType_WhenEntityExists() {
        when(loanTypeReactiveRepository.findByName(DomainConstants.MICROCREDIT))
                .thenReturn(Mono.just(loanTypeEntity));
        when(loanTypeMapper.toDomain(loanTypeEntity)).thenReturn(type);

        StepVerifier.create(loanTypeRepositoryAdapter.findByName(DomainConstants.MICROCREDIT))
                .expectNext(type)
                .verifyComplete();

        verify(loanTypeReactiveRepository).findByName(DomainConstants.MICROCREDIT);
        verify(loanTypeMapper).toDomain(loanTypeEntity);
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenEntityDoesNotExist() {
        when(loanTypeReactiveRepository.findByName("Unknown"))
                .thenReturn(Mono.empty());

        StepVerifier.create(loanTypeRepositoryAdapter.findByName("Unknown"))
                .verifyComplete();

        verify(loanTypeReactiveRepository).findByName("Unknown");
        verify(loanTypeMapper, never()).toDomain(any());
    }
}
