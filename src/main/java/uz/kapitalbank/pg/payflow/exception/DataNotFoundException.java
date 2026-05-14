package uz.kapitalbank.pg.payflow.exception;

import org.springframework.http.HttpStatus;
import uz.kapitalbank.pg.payflow.constant.error.ErrorType;

import static uz.kapitalbank.pg.payflow.constant.error.Error.DATA_NOT_FOUND_ERROR_CODE;

public class DataNotFoundException extends ApplicationException {

    public DataNotFoundException(String message) {
        super(DATA_NOT_FOUND_ERROR_CODE.getCode(), message, ErrorType.INTERNAL, HttpStatus.NOT_FOUND);
    }
}
