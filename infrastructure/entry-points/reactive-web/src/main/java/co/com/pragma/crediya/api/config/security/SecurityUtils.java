package co.com.pragma.crediya.api.config.security;

import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.jwt.gateways.JwtProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtProviderPort jwtProviderPort;

    public Mono<Jwt> getJwt() {
        return getCurrentToken()
                .map(jwtProviderPort::parseToken);
    }

    public Mono<String> getCurrentToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getCredentials)
                .cast(String.class);
    }

}