package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.r2dbc.entity.LoanStatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanStatusMapper {

    Status toDomain(LoanStatusEntity loanStatusEntity);

    LoanStatusEntity toEntity(Status status);

}
