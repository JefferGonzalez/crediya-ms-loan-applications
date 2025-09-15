package co.com.pragma.crediya.jwt;

import co.com.pragma.crediya.jwt.config.JwtProperties;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.user.User;
import co.com.pragma.crediya.model.user.constants.UserFieldNames;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderAdapterTest {

    private JwtProviderAdapter adapter;

    private User user;

    private final JwtProperties properties = new JwtProperties();

    @BeforeEach
    void setUp() {
        properties.setSecretKey("q/MbiTiaKL9wCSeISqOlOQvDjg7s+xmYRtNhYbq7T3A=");
        properties.setExpiration(10000L);

        adapter = new JwtProviderAdapter(properties);

        user = new User("123456789", "johndoe@example.com", BigDecimal.valueOf(100000));
    }

    @Test
    void parseToken_shouldReturnJwt() {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.email())
                .claim(UserFieldNames.ROLES, List.of(DomainConstants.ADMIN_ROLE))
                .claim(UserFieldNames.IDENTIFICATION_NUMBER, user.identificationNumber())
                .claim(UserFieldNames.BASE_SALARY, user.baseSalary())
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecretKey())))
                .compact();

        Jwt jwt = adapter.parseToken(token);

        assertThat(jwt).isNotNull();
        assertThat(jwt.subject()).isEqualTo(user.email());
        assertThat(jwt.roles()).containsExactly(DomainConstants.ADMIN_ROLE);
        assertThat(jwt.identificationNumber()).isEqualTo(user.identificationNumber());
        assertThat(jwt.baseSalary()).isEqualTo(user.baseSalary());
    }

}