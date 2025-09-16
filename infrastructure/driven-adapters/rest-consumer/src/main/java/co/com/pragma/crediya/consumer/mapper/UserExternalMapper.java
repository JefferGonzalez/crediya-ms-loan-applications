package co.com.pragma.crediya.consumer.mapper;

import co.com.pragma.crediya.consumer.dto.UserResponse;
import co.com.pragma.crediya.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserExternalMapper {

    User toDomain(UserResponse userResponse);

}
