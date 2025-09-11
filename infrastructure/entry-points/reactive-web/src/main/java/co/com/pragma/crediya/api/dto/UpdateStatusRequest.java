package co.com.pragma.crediya.api.dto;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.common.constants.ValidationErrorMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UpdateStatusRequest", description = "Request body for approve or reject a loan application")
public class UpdateStatusRequest {

    @NotBlank(message = ValidationErrorMessages.STATUS_REQUIRED)
    @Schema(
            description = "Status of the loan application",
            example = DomainConstants.APPROVED_STATUS,
            allowableValues = {DomainConstants.APPROVED_STATUS, DomainConstants.REJECTED_STATUS}
    )
    private String status;

}

