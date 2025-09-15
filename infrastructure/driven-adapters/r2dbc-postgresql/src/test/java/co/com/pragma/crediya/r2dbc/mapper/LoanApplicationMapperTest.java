package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LoanApplicationMapperImpl.class})
class LoanApplicationMapperTest {

    @Autowired
    private LoanApplicationMapper mapper;

    @Test
    void toDomain_fromEntity() {
        LoanApplicationEntity entity = LoanApplicationEntity.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1000))
                .term(12)
                .identificationNumber("123456789")
                .email("johndoe@example.com")
                .typeId(UUID.randomUUID())
                .statusId(UUID.randomUUID())
                .build();

        Application application = mapper.toDomain(entity);

        assertThat(application).isNotNull();
        assertThat(application.id()).isEqualTo(entity.getId());
        assertThat(application.amount()).isEqualTo(entity.getAmount());
        assertThat(application.term()).isEqualTo(entity.getTerm());
        assertThat(application.identificationNumber()).isEqualTo(entity.getIdentificationNumber());
        assertThat(application.email()).isEqualTo(entity.getEmail());
    }

    @Test
    void toDomain_fromProjection() {
        LoanApplicationProjection projection = new LoanApplicationProjection(UUID.randomUUID(), BigDecimal.valueOf(2000), 24, "johndoe@example.com", DomainConstants.MICROCREDIT, BigDecimal.valueOf(5.5), DomainConstants.DEFAULT_PENDING_STATUS);

        ApplicationReport report = mapper.toDomain(projection);

        assertThat(report).isNotNull();
        assertThat(report.id()).isEqualTo(projection.id());
        assertThat(report.type()).isEqualTo(projection.type());
        assertThat(report.interestRate()).isEqualTo(projection.interestRate());
        assertThat(report.status()).isEqualTo(projection.status());
    }

    @Test
    void toEntity_fromApplication() {
        Type type = new Type(UUID.randomUUID(), DomainConstants.MICROCREDIT, BigDecimal.valueOf(1000), BigDecimal.valueOf(2000), 12, 34, null, null);
        Status status = new Status(UUID.randomUUID(), DomainConstants.DEFAULT_PENDING_STATUS, "Default status");

        Application application = new Application(UUID.randomUUID(), BigDecimal.valueOf(1500), 18, "123456789", "johndoe@example.com", type, status);

        LoanApplicationEntity entity = mapper.toEntity(application);

        assertThat(entity).isNotNull();
        assertThat(entity.getTypeId()).isEqualTo(type.id());
        assertThat(entity.getStatusId()).isEqualTo(status.id());
    }

}
