package uz.kapitalbank.pg.payflow.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferToProcess {
  Long transferId;
  Long fromAccount;
  Long toAccount;
  Long amount;
  Boolean fraudCheckPassed = false;
}
