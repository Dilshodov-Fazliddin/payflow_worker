package uz.kapitalbank.pg.payflow.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.kapitalbank.pg.payflow.dto.request.TransferCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.TransferResponse;
import uz.kapitalbank.pg.payflow.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class TransferController {

  TransferService transferService;

  @PostMapping
  public ResponseEntity<TransferResponse> createTransfer(
    @RequestBody TransferCreateRequest request) {
    TransferResponse response = transferService.transferCreate(request);
    return ResponseEntity.ok(response);
  }
}