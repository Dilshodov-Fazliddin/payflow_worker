package uz.kapitalbank.pg.payflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferResult {
  private String status;
  private String failReason;
}
