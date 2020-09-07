package tech.introduct.mailbox.web.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Value
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorInfo {
    String error;
    String field;
    String details;
    Object info;

    public ErrorInfo(Exception e) {
        this(UPPER_CAMEL.to(LOWER_UNDERSCORE, e.getClass().getSimpleName()), null,
                getRootCause(e).getMessage(), null);
    }

    public ErrorInfo(String error) {
        this(error, null);
    }

    public ErrorInfo(String error, String field) {
        this(error, field, null, null);
    }

    public ErrorInfoRuntimeException exception(HttpStatus status) {
        return new ErrorInfoRuntimeException(status, this);
    }

    public ErrorInfoRuntimeException badRequest() {
        return exception(HttpStatus.BAD_REQUEST);
    }

    public ErrorInfoRuntimeException unavailable() {
        return exception(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
