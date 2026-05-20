package uz.kapitalbank.pg.payflow.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.kapitalbank.pg.payflow.constant.error.ErrorType;
import uz.kapitalbank.pg.payflow.dto.request.LoginRequest;
import uz.kapitalbank.pg.payflow.dto.request.UserCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.AuthResponse;
import uz.kapitalbank.pg.payflow.entity.UserEntity;
import uz.kapitalbank.pg.payflow.exception.ApplicationException;
import uz.kapitalbank.pg.payflow.jwt.JwtService;
import uz.kapitalbank.pg.payflow.mapper.UserMapper;
import uz.kapitalbank.pg.payflow.repository.UserRepository;

import static uz.kapitalbank.pg.payflow.constant.error.Error.INTERNAL_SERVICE_ERROR_CODE;


@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    AuthenticationManager authenticationManager;
    UserDetailsService userDetailsService;
    UserMapper userMapper;
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPassportNumber(), request.getPassword())
        );
        var userDetails = userDetailsService.loadUserByUsername(request.getPassportNumber());
        var token = jwtService.generateToken(userDetails);
        return buildAuthResponse(token);
    }

    public AuthResponse register(UserCreateRequest request) {
        userExistCheck(request.getPassportNumber());
        UserEntity user = userMapper.toEntity(request);
        userRepository.save(user);
        var userDetails = userDetailsService.loadUserByUsername(user.getPassportNumber());
        var token = jwtService.generateToken(userDetails);
        return buildAuthResponse(token);
    }

    private AuthResponse buildAuthResponse(String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .build();
    }


    public void userExistCheck(String passportNumber){
        if (userRepository.existsByPassportNumber(passportNumber)) {
            throw new ApplicationException(
                    INTERNAL_SERVICE_ERROR_CODE.getCode(),
                    "User with passport number already exists",
                    ErrorType.INTERNAL,
                    HttpStatus.CONFLICT
            );
        }
    }
}
