package uz.kapitalbank.pg.payflow.exception;

public class TransferCanceledException extends RuntimeException{
  public TransferCanceledException(String message) {
    super(message);
  }
}
