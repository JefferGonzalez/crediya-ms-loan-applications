package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.ApplicationRiskEvaluation;
import reactor.core.publisher.Mono;

public interface LoanValidationPort {

    Mono<Void> validateLoanAutomatically(ApplicationRiskEvaluation applicationRiskEvaluation);

}
