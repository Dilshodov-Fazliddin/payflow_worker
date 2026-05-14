package uz.kapitalbank.pg.payflow.dto.response;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import uz.kapitalbank.pg.payflow.constant.enums.Currency;
import uz.kapitalbank.pg.payflow.constant.enums.TransferStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferResponse {

    Long id;
    Long fromUserId;
    Long toUserId;
    Long amount;
    Currency currency;
    TransferStatus transferStatus;
    String processInstanceId;
    String failureReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime completedAt;
}
