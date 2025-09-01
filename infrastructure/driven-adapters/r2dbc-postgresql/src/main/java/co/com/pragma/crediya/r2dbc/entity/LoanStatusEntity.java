package co.com.pragma.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("loan_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoanStatusEntity {

    @Id
    private UUID id;

    private String name;

    private String description;

}
