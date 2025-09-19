package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.ApprovedApplication;
import reactor.core.publisher.Mono;

public interface LoanApprovedEventPort {

    Mono<Void> sendLoanApprovedEvent(ApprovedApplication approvedApplication);

}
