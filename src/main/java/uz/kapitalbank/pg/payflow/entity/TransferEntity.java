package uz.kapitalbank.pg.payflow.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import uz.kapitalbank.pg.payflow.constant.enums.Currency;
import uz.kapitalbank.pg.payflow.constant.enums.TransferStatus;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferEntity extends BaseEntity{
    @ManyToOne(fetch = FetchType.EAGER)
    UserEntity fromAccount;
    @ManyToOne(fetch = FetchType.EAGER)
    UserEntity toAccount;
    Long amount;
    @Enumerated(EnumType.STRING)
    Currency currency;
    @Enumerated(EnumType.STRING)
    TransferStatus transferStatus;
    String processInstanceId;
    LocalDateTime completedAt;
    String failureReason;
}
