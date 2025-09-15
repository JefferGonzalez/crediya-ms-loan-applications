package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LoanApplicationRestMapperImpl.class})
class LoanApplicationRestMapperTest {

    @Autowired
    private LoanApplicationRestMapper mapper;

    @Test
    void toDomain_shouldMapSaveLoanApplicationRequestToApplication() {
        SaveLoanApplicationRequest request = SaveLoanApplicationRequest.builder()
                .amount("1000.50")
                .term("12")
                .type(DomainConstants.MICROCREDIT)
                .build();

        Application application = mapper.toDomain(request);

        assertThat(application).isNotNull();
        assertThat(application.amount()).isEqualTo(request.getAmount());
        assertThat(application.term()).isEqualTo(Integer.parseInt(request.getTerm()));
        assertThat(application.type().name()).isEqualTo(request.getType());
    }

    @Test
    void toResponse_shouldMapApplicationToLoanApplicationResponse() {
        Type type = new Type(null, DomainConstants.MICROCREDIT, null, null, 0, 0, null, null);
        Status status = new Status(null, "APPROVED", null);
        Application application = new Application(UUID.randomUUID(), BigDecimal.valueOf(1000.50), 12, "123456", "jonhdoe@example.com", type, status);

        LoanApplicationResponse response = mapper.toResponse(application);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(application.id());
        assertThat(response.getAmount()).isEqualTo(application.amount());
        assertThat(response.getTerm()).isEqualTo(application.term());
        assertThat(response.getIdentificationNumber()).isEqualTo(application.identificationNumber());
        assertThat(response.getEmail()).isEqualTo(application.email());
        assertThat(response.getType()).isEqualTo(application.type().name());
        assertThat(response.getStatus()).isEqualTo(application.status().name());
    }

}
