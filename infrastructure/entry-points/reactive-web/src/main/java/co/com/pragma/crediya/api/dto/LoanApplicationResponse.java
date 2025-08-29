package co.com.pragma.crediya.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {

    private UUID id;

    private BigDecimal amount;

    private String term;

    private String identificationNumber;

    private String email;

    private String type;

    private String status;

}
