package uz.kapitalbank.pg.payflow.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import uz.kapitalbank.pg.payflow.constant.enums.FraudDecision;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FraudCheckResult {
  FraudDecision decision;
  String reason;
  long recentCount;
}
