package uz.kapitalbank.pg.payflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;

import java.time.LocalDateTime;

public interface TransferRepository extends JpaRepository<TransferEntity,Long> {


  @Query("""
           SELECT COUNT(t)
           FROM TransferEntity t
           WHERE t.fromAccount.id = :fromAccountId
             AND t.createdAt >= :since
           """)
  long countByAccountSince(@Param("fromAccountId") Long fromAccountId,
                           @Param("since") LocalDateTime since);
}
