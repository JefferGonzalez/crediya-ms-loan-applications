package co.com.pragma.crediya.api.dto;


import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.common.constants.ValidationConstants;
import co.com.pragma.crediya.model.common.constants.ValidationErrorMessages;
import co.com.pragma.crediya.model.common.constants.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = ValidationErrorMessages.AMOUNT_REQUIRED)
    @Pattern(regexp = ValidationPatterns.DECIMAL_REGEX, message = ValidationErrorMessages.INVALID_DECIMAL_FORMAT)
    @Schema(description = "Loan application's amount.", example = "123456.78")
    private String amount;

    @NotBlank(message = ValidationErrorMessages.TERM_REQUIRED)
    @Pattern(regexp = ValidationPatterns.TERM_REGEX, message = ValidationErrorMessages.TERM_RANGE)
    @Schema(description = "Loan application's term in months.", example = "12")
    private String term;

    @NotBlank(message = ValidationErrorMessages.IDENTIFICATION_NUMBER_REQUIRED)
    @Size(min = ValidationConstants.IDENTIFICATION_NUMBER_LENGTH, max = ValidationConstants.IDENTIFICATION_NUMBER_LENGTH, message = ValidationErrorMessages.IDENTIFICATION_NUMBER_LENGTH)
    @Pattern(regexp = ValidationPatterns.IDENTIFICATION_NUMBER_REGEX, message = ValidationErrorMessages.INVALID_IDENTIFICATION_NUMBER_FORMAT)
    @Schema(description = "User's identification number", example = "1234567890")
    private String identificationNumber;

    @NotBlank(message = ValidationErrorMessages.TYPE_REQUIRED)
    @Schema(description = "Loan Type.", example = DomainConstants.MICROCREDIT)
    private String type;

    public BigDecimal getAmount() {
        return new BigDecimal(amount);
    }

}