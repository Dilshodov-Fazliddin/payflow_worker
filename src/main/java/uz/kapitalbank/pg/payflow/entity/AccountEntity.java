package uz.kapitalbank.pg.payflow.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import uz.kapitalbank.pg.payflow.constant.enums.AccountStatus;
import uz.kapitalbank.pg.payflow.constant.enums.Currency;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountEntity extends BaseEntity{
    @Column(nullable = false)
    Long balance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    AccountStatus accountStatus;

    @Column(nullable = false)
    Long dailyLimitUsed;

    @Column(nullable = false)
    Long dailyLimitMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;
}