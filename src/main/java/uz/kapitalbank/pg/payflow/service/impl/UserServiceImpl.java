package uz.kapitalbank.pg.payflow.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import uz.kapitalbank.pg.payflow.entity.UserEntity;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.repository.UserRepository;
import uz.kapitalbank.pg.payflow.service.UserService;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public UserEntity findByUserId(Long userId) {
        return userRepository
                .findById(userId).orElseThrow(()-> new DataNotFoundException("User not found with id:" + userId));
    }
}
