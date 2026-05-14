package uz.kapitalbank.pg.payflow.mapper;


import org.mapstruct.*;
import uz.kapitalbank.pg.payflow.dto.request.UserCreateRequest;
import uz.kapitalbank.pg.payflow.dto.request.UserUpdateRequest;
import uz.kapitalbank.pg.payflow.dto.response.UserResponse;
import uz.kapitalbank.pg.payflow.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passportNumber", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget UserEntity entity);
}
