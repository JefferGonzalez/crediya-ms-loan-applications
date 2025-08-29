package co.com.pragma.crediya.api.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(
        name = "ProblemDetails",
        description = "Standardized error response body following RFC 7807 style"
)
public class ProblemDetails {

    @Schema(description = "Short, human-readable title of the error", example = "Invalid Request")
    private String title;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @JsonIgnore
    private HttpStatus httpStatus;

    @Schema(
            description = "List of field-level validation errors (if any)",
            implementation = FieldValidationError.class
    )
    private List<FieldValidationError> errors;

    @Schema(description = "Timestamp when the error occurred", example = "2025-08-27 15:45:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ProblemDetails(String title, HttpStatus status, List<FieldValidationError> errors) {
        this.title = title;
        this.httpStatus = status;
        this.status = status.value();
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public static ProblemDetails badRequest(String title, List<FieldValidationError> errors) {
        return new ProblemDetails(title, HttpStatus.BAD_REQUEST, errors);
    }

    public static ProblemDetails conflict(String title, List<FieldValidationError> errors) {
        return new ProblemDetails(title, HttpStatus.CONFLICT, errors);
    }

    @JsonIgnore
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
