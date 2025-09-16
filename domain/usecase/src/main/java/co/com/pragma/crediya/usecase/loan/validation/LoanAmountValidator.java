package co.com.pragma.crediya.usecase.loan.validation;

import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationValueOutOfBoundsException;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public record LoanAmountValidator(LoggerPort logger) {

    public Mono<ValidationOutcome> validate(Application application) {
        BigDecimal amount = application.amount();
        BigDecimal minValue = application.type().minimumAmount();
        BigDecimal maxValue = application.type().maximumAmount();

        if (amount.compareTo(minValue) < 0 || amount.compareTo(maxValue) > 0) {
            ApplicationValueOutOfBoundsException ex = new ApplicationValueOutOfBoundsException(minValue, maxValue);
            logger.error("Loan amount validation failed. Amount: {}, Min: {}, Max: {}", amount, minValue, maxValue, ex);

            return Mono.just(ValidationOutcome.error(ApplicationFieldNames.AMOUNT, ex.getMessage()));
        }

        logger.info("Loan amount validation passed. Amount: {}, Min: {}, Max: {}", amount, minValue, maxValue);
        return Mono.just(ValidationOutcome.success(ApplicationFieldNames.AMOUNT));
    }

}
