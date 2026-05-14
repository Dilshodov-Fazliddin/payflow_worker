package uz.kapitalbank.pg.payflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.kapitalbank.pg.payflow.constant.enums.AccountStatus;
import uz.kapitalbank.pg.payflow.entity.AccountEntity;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity,Long> {
  Optional<AccountEntity> findByIdAndAccountStatus(Long id, AccountStatus accountStatus);

}
