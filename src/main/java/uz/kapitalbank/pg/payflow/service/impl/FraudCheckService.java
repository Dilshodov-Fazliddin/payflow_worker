package uz.kapitalbank.pg.payflow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.kapitalbank.pg.payflow.constant.enums.FraudDecision;
import uz.kapitalbank.pg.payflow.dto.response.FraudCheckResult;
import uz.kapitalbank.pg.payflow.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FraudCheckService {

  private static final int FREQUENCY_THRESHOLD = 10;
  private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("100000");
  private static final BigDecimal REJECT_THRESHOLD = new BigDecimal("1000000");
  private static final int FREQUENCY_WINDOW_HOURS = 1;

  TransferRepository transferRepository;

  public FraudCheckResult check(Long accountId, BigDecimal amount) {
    LocalDateTime since = LocalDateTime.now().minusHours(FREQUENCY_WINDOW_HOURS);
    long recentCount = transferRepository.countByAccountSince(accountId, since);
    boolean frequencySuspicious = recentCount > FREQUENCY_THRESHOLD;

    log.debug("Fraud signals: account={}, amount={}, recentCount={}, suspiciousFreq={}",
      accountId, amount, recentCount, frequencySuspicious);

    if (amount.compareTo(REJECT_THRESHOLD) > 0) {
      return new FraudCheckResult(
        FraudDecision.REJECTED,
        "Amount exceeds hard reject threshold (" + REJECT_THRESHOLD + ")",
        recentCount
      );
    }

    if (frequencySuspicious && amount.compareTo(REVIEW_THRESHOLD) > 0) {
      return new FraudCheckResult(
        FraudDecision.REJECTED,
        "Suspicious frequency combined with large amount",
        recentCount
      );
    }

    if (amount.compareTo(REVIEW_THRESHOLD) > 0) {
      return new FraudCheckResult(
        FraudDecision.REVIEW_REQUIRED,
        "Amount exceeds review threshold (" + REVIEW_THRESHOLD + ")",
        recentCount
      );
    }

    if (frequencySuspicious) {
      return new FraudCheckResult(
        FraudDecision.REVIEW_REQUIRED,
        "Suspicious transfer frequency: " + recentCount + " in last hour",
        recentCount
      );
    }

    return new FraudCheckResult(
      FraudDecision.APPROVED,
      "No fraud signals detected",
      recentCount
    );
  }
}