package uz.kapitalbank.pg.payflow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.kapitalbank.pg.payflow.constant.enums.TransferStatus;
import uz.kapitalbank.pg.payflow.dto.request.TransferCreateRequest;
import uz.kapitalbank.pg.payflow.dto.request.TransferToProcess;
import uz.kapitalbank.pg.payflow.dto.response.TransferResponse;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;

@Mapper(componentModel = "spring", imports = {TransferStatus.class})
public interface TransferMapper {

    @Mapping(source = "fromAccount.id", target = "fromUserId")
    @Mapping(source = "toAccount.id", target = "toUserId")
    TransferResponse toResponse(TransferEntity entity);

    @Mapping(source = "fromAccount.id", target = "fromAccount")
    @Mapping(source = "toAccount.id", target = "toAccount")
    @Mapping(source = "id", target = "transferId")
    TransferToProcess toProcess(TransferEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "fromAccount", ignore = true)
    @Mapping(target = "toAccount", ignore = true)
    @Mapping(target = "transferStatus", expression = "java(TransferStatus.STARTED)")
    @Mapping(target = "processInstanceId", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    TransferEntity toEntity(TransferCreateRequest request);
}