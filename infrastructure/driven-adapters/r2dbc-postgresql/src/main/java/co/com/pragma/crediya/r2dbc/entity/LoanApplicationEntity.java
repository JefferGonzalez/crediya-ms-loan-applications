package co.com.pragma.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("loan_application")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoanApplicationEntity {

    @Id
    private UUID id;

    private BigDecimal amount;

    private String term;

    private String identificationNumber;

    private String email;

    private UUID typeId;

    private UUID statusId;

}
