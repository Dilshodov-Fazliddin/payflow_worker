package uz.kapitalbank.pg.payflow.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import uz.kapitalbank.pg.payflow.constant.enums.AccountStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountUpdateRequest {

    @Min(value = 1, message = "Daily limit must be at least 1")
    Integer dailyLimitMax;

    AccountStatus accountStatus;
}
