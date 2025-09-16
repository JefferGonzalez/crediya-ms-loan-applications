package co.com.pragma.crediya.usecase.loan.validation;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class LoanAmountValidatorTest {

    @Mock
    private LoggerPort logger;

    private LoanAmountValidator validator;

    private Type type;

    private Status status;

    private Application application;

    @BeforeEach
    void setUp() {
        type = new Type(UUID.randomUUID(), DomainConstants.MICROCREDIT, BigDecimal.valueOf(300000), BigDecimal.valueOf(50000000), 12, 24, BigDecimal.valueOf(25.00), true);
        status = new Status(UUID.randomUUID(), DomainConstants.DEFAULT_PENDING_STATUS, "Application received, under evaluation");

        application = new Application(UUID.randomUUID(), BigDecimal.valueOf(4000000), 12, "1234567890", "johndoe@example.com", type, status);

        validator = new LoanAmountValidator(logger);
    }

    @Test
    void shouldReturnErrorWhenAmountIsLessThanMinimum() {
        Application invalidApp = new Application(application.id(), BigDecimal.valueOf(200000), application.term(), application.identificationNumber(), application.email(), type, status);

        ValidationOutcome outcome = validator.validate(invalidApp).block();

        Assertions.assertNotNull(outcome);

        assertThat(outcome.isValid()).isFalse();

        assertThat(outcome.field()).isEqualTo(ApplicationFieldNames.AMOUNT);

        assertThat(outcome.errorMessage()).contains(ApplicationConstants.MOST_BE_BETWEEN);
    }

    @Test
    void shouldReturnErrorWhenAmountIsGreaterThanMaximum() {
        Application invalidApp = new Application(application.id(), BigDecimal.valueOf(60000000), application.term(), application.identificationNumber(), application.email(), type, status);

        ValidationOutcome outcome = validator.validate(invalidApp).block();

        Assertions.assertNotNull(outcome);

        assertThat(outcome.isValid()).isFalse();

        assertThat(outcome.field()).isEqualTo(ApplicationFieldNames.AMOUNT);

        assertThat(outcome.errorMessage()).contains(ApplicationConstants.MOST_BE_BETWEEN);
    }


    @Test
    void shouldReturnSuccessWhenAmountIsWithinRange() {
        ValidationOutcome outcome = validator.validate(application).block();

        Assertions.assertNotNull(outcome);

        assertThat(outcome.isValid()).isTrue();

        assertThat(outcome.errorMessage()).isNull();
    }

}

