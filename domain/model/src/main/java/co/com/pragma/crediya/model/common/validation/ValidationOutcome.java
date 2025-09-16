package co.com.pragma.crediya.model.common.validation;

public record ValidationOutcome(boolean isValid, String field, String errorMessage) {

    public static ValidationOutcome success(String field) {
        return new ValidationOutcome(true, field, null);
    }

    public static ValidationOutcome error(String field, String message) {
        return new ValidationOutcome(false, field, message);
    }

}