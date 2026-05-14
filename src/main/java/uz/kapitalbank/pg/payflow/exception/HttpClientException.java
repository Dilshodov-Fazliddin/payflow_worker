package uz.kapitalbank.pg.payflow.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpClientException extends RuntimeException {

    private final HttpStatus status;

    public HttpClientException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
