package uz.kapitalbank.pg.payflow.service;


import uz.kapitalbank.pg.payflow.entity.UserEntity;

public interface UserService {

    UserEntity findByUserId(Long userId);

}
