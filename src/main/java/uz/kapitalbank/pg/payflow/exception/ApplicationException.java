package uz.kapitalbank.pg.payflow.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import uz.kapitalbank.pg.payflow.constant.error.ErrorType;

@Getter
public class ApplicationException extends RuntimeException {

    private final int code;
    private final ErrorType errorType;
    private final HttpStatus status;

    public ApplicationException(int code, String message, ErrorType errorType, HttpStatus status) {
        super(message);
        this.code = code;
        this.errorType = errorType;
        this.status = status;
    }
}
