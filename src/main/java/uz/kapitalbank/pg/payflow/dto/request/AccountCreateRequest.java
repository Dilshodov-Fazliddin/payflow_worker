package uz.kapitalbank.pg.payflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import uz.kapitalbank.pg.payflow.constant.enums.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreateRequest {

    @NotNull(message = "Currency is required")
    Currency currency;

    @NotNull(message = "Daily limit is required")
    @Min(value = 1, message = "Daily limit must be at least 1")
    Long dailyLimitMax;

    @NotNull(message = "User ID is required")
    Long userId;
}
