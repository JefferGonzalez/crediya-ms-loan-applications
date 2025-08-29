package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.r2dbc.entity.LoanTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanTypeMapper {

    Type toDomain(LoanTypeEntity loanTypeEntity);

    LoanTypeEntity toEntity(Type type);

}
