package uz.kapitalbank.pg.payflow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.kapitalbank.pg.payflow.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDetails implements org.springframework.security.core.userdetails.UserDetailsService {

  UserRepository userRepository;

  @Override
  public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String passportNumber) throws UsernameNotFoundException {
    var user = userRepository.findByPassportNumber(passportNumber)
      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + passportNumber));

    return User.withUsername(user.getPassportNumber())
      .password(user.getPassword())
      .build();
  }
}
