package co.com.pragma.crediya.usecase.loan.validation;

import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.loan.Application;
import reactor.core.publisher.Mono;

import java.util.List;

public record ValidationLoanApplicationOrchestrator(
        LoanAmountValidator loanAmountValidator,
        LoanTermValidator loanTermValidator) {

    public Mono<List<ValidationOutcome>> validateApplicationBusinessRules(Application application) {
        return Mono.zip(
                loanAmountValidator.validate(application),
                loanTermValidator.validate(application)
        ).map(tuple -> List.of(tuple.getT1(), tuple.getT2()));
    }

}