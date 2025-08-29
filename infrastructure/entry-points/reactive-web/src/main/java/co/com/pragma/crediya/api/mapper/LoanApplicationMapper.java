package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Type;

public class LoanApplicationMapper {

    private LoanApplicationMapper() {
    }

    public static Application toDomain(SaveLoanApplicationRequest application) {
        Type type = new Type(null, application.getType(), null, null, null, null);
        return new Application(null, application.getAmount(), application.getTerm(), application.getIdentificationNumber(), application.getEmail(), type, null);
    }

    public static LoanApplicationResponse toResponse(Application application) {

        return LoanApplicationResponse.builder()
                .id(application.id())
                .amount(application.amount())
                .term(application.term())
                .identificationNumber(application.identificationNumber())
                .email(application.email())
                .type(application.type().name())
                .status(application.status().name())
                .build();

    }

}
