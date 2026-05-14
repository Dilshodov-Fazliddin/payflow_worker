package uz.kapitalbank.pg.payflow.controller;


import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.kapitalbank.pg.payflow.dto.request.AccountCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.AccountResponse;
import uz.kapitalbank.pg.payflow.service.AccountService;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AccountController {

    AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse>createAccount(@RequestBody @Valid AccountCreateRequest accountCreateRequest){
        return ResponseEntity.ok(accountService.createAccount(accountCreateRequest));
    }
}
