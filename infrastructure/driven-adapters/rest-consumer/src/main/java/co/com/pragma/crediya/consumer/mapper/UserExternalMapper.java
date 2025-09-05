package co.com.pragma.crediya.consumer.mapper;

import co.com.pragma.crediya.consumer.dto.UserResponse;
import co.com.pragma.crediya.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserExternalMapper {

    @Mapping(target = "identificationNumber", ignore = true)
    User toDomain(UserResponse userResponse);

}
