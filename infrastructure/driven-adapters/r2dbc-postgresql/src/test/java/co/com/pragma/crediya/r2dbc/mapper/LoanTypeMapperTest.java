package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.r2dbc.entity.LoanTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LoanTypeMapperImpl.class})
class LoanTypeMapperTest {

    @Autowired
    private LoanTypeMapper mapper;

    @Test
    void toDomain_fromEntity() {
        LoanTypeEntity entity = LoanTypeEntity.builder()
                .id(UUID.randomUUID())
                .name(DomainConstants.MICROCREDIT)
                .minimumAmount(BigDecimal.valueOf(1000))
                .maximumAmount(BigDecimal.valueOf(5000))
                .minimumTerm(12)
                .maximumTerm(36)
                .interestRate(BigDecimal.valueOf(5.5))
                .automaticValidation(true)
                .build();

        Type type = mapper.toDomain(entity);

        assertThat(type).isNotNull();
        assertThat(type.id()).isEqualTo(entity.getId());
        assertThat(type.name()).isEqualTo(entity.getName());
        assertThat(type.minimumAmount()).isEqualTo(entity.getMinimumAmount());
        assertThat(type.maximumAmount()).isEqualTo(entity.getMaximumAmount());
        assertThat(type.minimumTerm()).isEqualTo(entity.getMinimumTerm());
        assertThat(type.maximumTerm()).isEqualTo(entity.getMaximumTerm());
        assertThat(type.interestRate()).isEqualTo(entity.getInterestRate());
        assertThat(type.automaticValidation()).isEqualTo(entity.getAutomaticValidation());
    }

}

