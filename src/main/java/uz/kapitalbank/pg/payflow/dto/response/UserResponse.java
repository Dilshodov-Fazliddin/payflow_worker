package uz.kapitalbank.pg.payflow.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    Long id;
    String firstName;
    String lastName;
    String passportNumber;
    Integer age;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
