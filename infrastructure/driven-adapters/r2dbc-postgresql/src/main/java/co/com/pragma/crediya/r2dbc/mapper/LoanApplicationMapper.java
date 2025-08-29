package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.r2dbc.entity.LoanApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LoanTypeMapper.class, LoanStatusMapper.class})
public interface LoanApplicationMapper {

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    Application toDomain(LoanApplicationEntity loanApplicationEntity);

    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "status.id", target = "statusId")
    LoanApplicationEntity toEntity(Application application);

}