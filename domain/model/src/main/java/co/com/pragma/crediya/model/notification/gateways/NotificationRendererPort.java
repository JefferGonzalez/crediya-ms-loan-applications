package co.com.pragma.crediya.model.notification.gateways;

import co.com.pragma.crediya.model.StatusChange;
import co.com.pragma.crediya.model.notification.LoanApproval;
import reactor.core.publisher.Mono;

public interface NotificationRendererPort {

    Mono<String> processLoanApprovalTemplate(LoanApproval approval);

    Mono<String> processStatusChangeTemplate(StatusChange statusChange);

}

