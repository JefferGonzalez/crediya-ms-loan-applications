package co.com.pragma.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("loan_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoanTypeEntity {

    @Id
    private UUID id;

    private String name;

    private BigDecimal minimumAmount;

    private BigDecimal maximumAmount;

    private BigDecimal interestRate;

    private Boolean automaticValidation;

}
