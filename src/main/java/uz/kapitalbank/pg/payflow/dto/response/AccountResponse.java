package uz.kapitalbank.pg.payflow.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import uz.kapitalbank.pg.payflow.constant.enums.AccountStatus;
import uz.kapitalbank.pg.payflow.constant.enums.Currency;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    Long id;
    Long balance;
    Currency currency;
    AccountStatus accountStatus;
    Long dailyLimitUsed;
    Long dailyLimitMax;
    Long userId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
