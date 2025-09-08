package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.r2dbc.entity.LoanStatusEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LoanStatusMapperImpl.class})
class LoanStatusMapperTest {

    @Autowired
    private LoanStatusMapper mapper;

    @Test
    void toDomain_fromEntity() {
        LoanStatusEntity entity = LoanStatusEntity.builder()
                .id(UUID.randomUUID())
                .name(DomainConstants.DEFAULT_PENDING_STATUS)
                .description("Default status")
                .build();

        Status status = mapper.toDomain(entity);

        assertThat(status).isNotNull();
        assertThat(status.id()).isEqualTo(entity.getId());
        assertThat(status.name()).isEqualTo(entity.getName());
        assertThat(status.description()).isEqualTo(entity.getDescription());
    }

}

