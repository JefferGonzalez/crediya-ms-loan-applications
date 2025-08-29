package co.com.pragma.crediya.api.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class ReactiveValidator {

    private final Validator validator;

    public ReactiveValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> Mono<T> validate(T object) {
        return Mono.defer(() -> {
            Set<ConstraintViolation<T>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                return Mono.error(new ConstraintViolationException(violations));
            }

            return Mono.just(object);
        });

    }

}
