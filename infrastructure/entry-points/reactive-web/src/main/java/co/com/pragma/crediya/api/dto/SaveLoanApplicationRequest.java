package co.com.pragma.crediya.api.dto;


import co.com.pragma.crediya.model.loan.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.exceptions.ErrorMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SaveLoanApplicationRequest", description = "Request body for creating a new loan application")
public class SaveLoanApplicationRequest {

    @NotBlank(message = "Amount can't be empty")
    @DecimalMin(value = "1.00", message = "Minimum value depends on loan type")
    @DecimalMax(value = "999999999999999999.99", message = "Maximum value depends on loan type")
    @Pattern(regexp = RegexPatterns.DECIMAL_REGEX, message = ErrorMessages.INVALID_DECIMAL_FORMAT)
    @Schema(description = "Loan application's amount.", example = "123456.78")
    private String amount;

    @NotBlank(message = "Term can't be empty")
    @Schema(description = "Loan application's deadline in months.", example = "12")
    private String term;

    @NotBlank(message = ErrorMessages.IDENTIFICATION_NUMBER_REQUIRED)
    @Size(min = DomainConstants.IDENTIFICATION_NUMBER_LENGTH, max = DomainConstants.IDENTIFICATION_NUMBER_LENGTH, message = ErrorMessages.IDENTIFICATION_NUMBER_LENGTH)
    @Pattern(regexp = RegexPatterns.IDENTIFICATION_NUMBER_REGEX, message = ErrorMessages.INVALID_IDENTIFICATION_NUMBER_FORMAT)
    @Schema(description = "User's identification number", example = "1234567890")
    private String identificationNumber;

    @NotBlank(message = "Email can't be empty")
    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Type can't be empty")
    @Schema(description = "Loan Type.")
    private String type;

    public BigDecimal getAmount() {
        return new BigDecimal(amount);
    }

}