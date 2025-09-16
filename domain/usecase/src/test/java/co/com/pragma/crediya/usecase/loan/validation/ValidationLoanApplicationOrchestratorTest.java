package co.com.pragma.crediya.usecase.loan.validation;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ValidationLoanApplicationOrchestratorTest {

    @Mock
    private LoanAmountValidator loanAmountValidator;

    @Mock
    private LoanTermValidator loanTermValidator;

    private ValidationLoanApplicationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new ValidationLoanApplicationOrchestrator(loanAmountValidator, loanTermValidator);
    }

    @Test
    void validate_shouldReturnValidationOutcomes() {
        Type type = new Type(UUID.randomUUID(), DomainConstants.MICROCREDIT, BigDecimal.valueOf(300000), BigDecimal.valueOf(50000000), 12, 24, BigDecimal.valueOf(25.00), true);
        Status status = new Status(UUID.randomUUID(), DomainConstants.DEFAULT_PENDING_STATUS, "Application received, under evaluation");

        Application application = new Application(UUID.randomUUID(), BigDecimal.valueOf(4000000), 12, "1234567890", "johndoe@example.com", type, status);

        ValidationOutcome amountOutcome = ValidationOutcome.success(ApplicationFieldNames.AMOUNT);
        ValidationOutcome termOutcome = ValidationOutcome.success(ApplicationFieldNames.TERM);

        Mockito.when(loanAmountValidator.validate(application)).thenReturn(Mono.just(amountOutcome));
        Mockito.when(loanTermValidator.validate(application)).thenReturn(Mono.just(termOutcome));

        StepVerifier.create(orchestrator.validateApplicationBusinessRules(application))
                .expectNextMatches(list ->
                        list.contains(amountOutcome) && list.contains(termOutcome)
                ).verifyComplete();
    }

}
