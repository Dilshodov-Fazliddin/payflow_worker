package uz.kapitalbank.pg.payflow.camunda.constant;

public class CamundaConstants {
  public static final String CHECK_ACCOUNT_TOPIC = "account-check";
  public static final String CHECK_AMOUNT_TOPIC = "amount-check";
  public static final String CHECK_DAILY_LIMIT_TOPIC = "limit-check";
  public static final String CHECK_FREQUENCY = "frequency-check";

  public static final String DEBIT_WORKER = "debit";
  public static final String CREDIT_WORKER = "credit";


  public static final String ROLLBACK_WORKER = "rollback";


  public static final String FROM_ACCOUNT = "fromAccount";
  public static final String TO_ACCOUNT = "toAccount";
  public static final String AMOUNT = "amount";
  public static final String TRANSFER_ID = "transferId";
  public static final String PROCESS_INSTANCE = "processInstance";


  public static final String FRAUD_CHECK_PASSED = "fraudCheckPassed";


}
