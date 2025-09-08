package co.com.pragma.crediya.consumer.mapper;

import co.com.pragma.crediya.consumer.dto.UserResponse;
import co.com.pragma.crediya.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserExternalMapperImpl.class})
class UserExternalMapperTest {

    @Autowired
    private UserExternalMapper mapper;

    @Test
    void toDomain_shouldMapUserResponseToUser() {
        UserResponse response = UserResponse.builder()
                .email("johndoe@example.com")
                .baseSalary(BigDecimal.valueOf(5000))
                .build();

        User user = mapper.toDomain(response);

        assertThat(user).isNotNull();
        assertThat(user.email()).isEqualTo(response.getEmail());
        assertThat(user.baseSalary()).isEqualByComparingTo(response.getBaseSalary());
        assertThat(user.identificationNumber()).isNull();
    }
}

