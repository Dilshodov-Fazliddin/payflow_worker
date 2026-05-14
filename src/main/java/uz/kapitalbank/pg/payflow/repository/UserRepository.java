package uz.kapitalbank.pg.payflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.kapitalbank.pg.payflow.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPassportNumber(String passportNumber);

    boolean existsByPassportNumber(String passportNumber);
}
