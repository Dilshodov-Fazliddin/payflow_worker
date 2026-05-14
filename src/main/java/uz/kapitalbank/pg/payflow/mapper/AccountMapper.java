package uz.kapitalbank.pg.payflow.mapper;


import org.mapstruct.*;
import uz.kapitalbank.pg.payflow.constant.enums.AccountStatus;
import uz.kapitalbank.pg.payflow.dto.request.AccountCreateRequest;
import uz.kapitalbank.pg.payflow.dto.request.AccountUpdateRequest;
import uz.kapitalbank.pg.payflow.dto.response.AccountResponse;
import uz.kapitalbank.pg.payflow.entity.AccountEntity;

@Mapper(componentModel = "spring", imports = {AccountStatus.class})
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")
    AccountResponse toResponse(AccountEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "balance", expression = "java(0L)")
    @Mapping(target = "dailyLimitUsed", expression = "java(0L)")
    @Mapping(target = "accountStatus", expression = "java(AccountStatus.NOT_ACTIVE)")
    AccountEntity toEntity(AccountCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "dailyLimitUsed", ignore = true)
    void updateEntity(AccountUpdateRequest request, @MappingTarget AccountEntity entity);
}
