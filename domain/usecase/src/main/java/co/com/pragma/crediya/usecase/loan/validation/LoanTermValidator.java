package co.com.pragma.crediya.usecase.loan.validation;

import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationTermOutOfBoundsException;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import reactor.core.publisher.Mono;

public record LoanTermValidator(LoggerPort logger) {

    public Mono<ValidationOutcome> validate(Application application) {
        int term = application.term();
        int minValue = application.type().minimumTerm();
        int maxValue = application.type().maximumTerm();

        if (term < minValue || term > maxValue) {
            ApplicationTermOutOfBoundsException ex = new ApplicationTermOutOfBoundsException(minValue, maxValue);
            logger.error("Loan term validation failed. Term: {}, Min: {}, Max: {}", term, minValue, maxValue, ex);

            return Mono.just(ValidationOutcome.error(ApplicationFieldNames.TERM, ex.getMessage()));
        }

        logger.info("Loan term validation passed. Term: {}, Min: {}, Max: {}", term, minValue, maxValue);
        return Mono.just(ValidationOutcome.success(ApplicationFieldNames.TERM));
    }

}