package co.com.pragma.crediya.r2dbc.mapper;

import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LoanTypeMapper.class, LoanStatusMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LoanApplicationMapper {

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    Application toDomain(LoanApplicationEntity loanApplicationEntity);

    ApplicationReport toDomain(LoanApplicationProjection loanApplicationProjection);

    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "status.id", target = "statusId")
    LoanApplicationEntity toEntity(Application application);

}