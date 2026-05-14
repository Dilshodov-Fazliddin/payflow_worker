package uz.kapitalbank.pg.payflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName;

    @Min(value = 18, message = "User must be at least 18 years old")
    @Max(value = 120, message = "Age must not exceed 120")
    Integer age;
}
