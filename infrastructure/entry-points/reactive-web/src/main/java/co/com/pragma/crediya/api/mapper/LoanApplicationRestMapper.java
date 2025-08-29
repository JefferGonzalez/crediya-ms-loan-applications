package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.model.loan.Application;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LoanTypeRestMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LoanApplicationRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "type", target = "type.name")
    Application toDomain(SaveLoanApplicationRequest dto);

    @Mapping(source = "type.name", target = "type")
    @Mapping(source = "status.name", target = "status")
    LoanApplicationResponse toResponse(Application application);

}

