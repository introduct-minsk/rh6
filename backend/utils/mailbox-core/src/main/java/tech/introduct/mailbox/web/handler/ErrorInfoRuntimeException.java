package tech.introduct.mailbox.web.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorInfoRuntimeException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final ErrorInfo errorInfo;

    ErrorInfoRuntimeException(HttpStatus httpStatus, ErrorInfo errorInfo) {
        super(errorInfo.getError());
        this.httpStatus = httpStatus;
        this.errorInfo = errorInfo;
    }
}
