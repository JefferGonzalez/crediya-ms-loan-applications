package co.com.pragma.crediya.api.exceptions.handler;

import co.com.pragma.crediya.api.constants.HttpErrorTitles;
import co.com.pragma.crediya.api.exceptions.*;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationCannotBeProcessedException;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationNotFoundException;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationValueOutOfBoundsException;
import co.com.pragma.crediya.model.loan.exceptions.TypeNotFoundException;
import co.com.pragma.crediya.model.user.exceptions.UserDataInconsistencyException;
import co.com.pragma.crediya.model.user.exceptions.UserGatewayException;
import co.com.pragma.crediya.model.user.exceptions.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ProblemDetails problemDetails = createProblemDetails(ex);

        response.setStatusCode(problemDetails.getHttpStatus());

        try {
            String responseBody = objectMapper.writeValueAsString(problemDetails);
            DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.writeWith(Mono.empty());
        }
    }

    private ProblemDetails createProblemDetails(Throwable ex) {
        if (ex instanceof ConstraintViolationException constraintEx) {
            return handleConstraintViolation(constraintEx);
        }

        if (ex instanceof WebExchangeBindException bindEx) {
            return handleWebExchangeBindException(bindEx);
        }

        if (ex instanceof EmptyRequestBodyException) {
            return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, ex.getMessage());
        }

        if (ex instanceof InvalidQueryParamException) {
            return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, ex.getMessage());
        }

        if (ex instanceof InvalidPathVariableException) {
            return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, ex.getMessage());
        }

        if (ex instanceof ApplicationValueOutOfBoundsException) {
            List<FieldValidationError> errors = List.of(new FieldValidationError(ApplicationFieldNames.AMOUNT, ex.getMessage()));
            return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, errors);
        }

        if (ex instanceof ApplicationCannotBeProcessedException) {
            List<FieldValidationError> errors = List.of(new FieldValidationError(ApplicationFieldNames.STATUS, ex.getMessage()));
            return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, errors);
        }

        if (ex instanceof TypeNotFoundException) {
            List<FieldValidationError> errors = List.of(new FieldValidationError(ApplicationFieldNames.TYPE, ex.getMessage()));
            return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, errors);
        }

        if (ex instanceof JwtAuthenticationException) {
            return ProblemDetails.unauthorized(HttpErrorTitles.UNAUTHORIZED, ex.getMessage());
        }

        if (ex instanceof UserNotFoundException) {
            return ProblemDetails.notFound(HttpErrorTitles.NOT_FOUND, ex.getMessage());
        }

        if (ex instanceof ApplicationNotFoundException) {
            return ProblemDetails.notFound(HttpErrorTitles.NOT_FOUND, ex.getMessage());
        }

        if (ex instanceof UserDataInconsistencyException) {
            List<FieldValidationError> errors = List.of(new FieldValidationError(ApplicationFieldNames.IDENTIFICATION_NUMBER, ex.getMessage()));
            return ProblemDetails.conflict(HttpErrorTitles.CONFLICT, errors);
        }

        if (ex instanceof UserGatewayException) {
            return ProblemDetails.internalSeverError(HttpErrorTitles.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

        return new ProblemDetails(HttpErrorTitles.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing your request. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    private ProblemDetails handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(this::mapConstraintViolation)
                .toList();

        return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, errors);
    }

    private ProblemDetails handleWebExchangeBindException(WebExchangeBindException ex) {
        List<FieldValidationError> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(fieldError ->
                        new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return ProblemDetails.badRequest(HttpErrorTitles.BAD_REQUEST, errors);
    }

    private FieldValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        String fieldName = violation.getPropertyPath().toString();
        String message = violation.getMessage();

        return new FieldValidationError(fieldName, message);
    }
}