package co.ravn.ecommerce.mappers.auth;

import co.ravn.ecommerce.dto.response.auth.LoginResponse;
import co.ravn.ecommerce.dto.response.auth.UserDetailsResponse;
import co.ravn.ecommerce.entities.auth.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(source = "person.firstName", target = "first_name")
    @Mapping(source = "person.lastName", target = "last_name")
    @Mapping(source = "person.email", target = "email")
    @Mapping(source = "person.phone", target = "phone")
    @Mapping(source = "person.id", target = "person_id")
    UserDetailsResponse toResponse(SysUser sysUser);

    @Mapping(source = "accessToken", target = "access_token")
    @Mapping(source = "refreshToken", target = "refresh_token")
    @Mapping(source = "sysUser.id", target = "id")
    @Mapping(source = "sysUser.person.email", target = "email")
    @Mapping(source = "sysUser.person.firstName", target = "firstName")
    @Mapping(source = "sysUser.person.lastName", target = "lastName")
    @Mapping(source = "sysUser.role.name", target = "role")
    LoginResponse toLoginResponse(SysUser sysUser, String accessToken, String refreshToken);
}
