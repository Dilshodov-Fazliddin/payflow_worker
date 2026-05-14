package uz.kapitalbank.pg.payflow.exception;

public class ProcessStartException extends RuntimeException {
  public ProcessStartException(String message, Throwable cause) {
    super(message, cause);
  }
}