package tech.introduct.mailbox.web.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.util.Optional;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ErrorInfoRuntimeException.class)
    public ResponseEntity<Object> handleAllException(ErrorInfoRuntimeException ex) {
        return new ResponseEntity<>(ex.getErrorInfo(), ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), null, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), BAD_REQUEST, request);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, @Nullable Object body,
                                                             @NonNull HttpHeaders headers, @Nullable HttpStatus status,
                                                             @NonNull WebRequest request) {
        if (status != null && !status.is5xxServerError()) {
            return new ResponseEntity<>(new ErrorInfo(ex), headers, status);
        }
        logger.error("internal_server_error", ex);
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        return new ResponseEntity<>(new ErrorInfo(ex), headers, INTERNAL_SERVER_ERROR);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleBindException(@NonNull BindException ex, @Nullable HttpHeaders headers,
                                                         @NonNull HttpStatus status, @NonNull WebRequest request) {
        return new ResponseEntity<>(getErrorInfoList(ex), headers, status);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @Nullable HttpHeaders headers,
                                                                  @NonNull HttpStatus status,
                                                                  @NonNull WebRequest request) {
        return new ResponseEntity<>(getErrorInfoList(ex.getBindingResult()), headers, status);
    }

    private ErrorInfo getErrorInfoList(BindingResult result) {
        return result.getAllErrors().stream()
                .map(error -> {
                    String field = Optional.of(error)
                            .filter(e -> e instanceof FieldError)
                            .map(e -> ((FieldError) e).getField())
                            .orElse(null);
                    var code = defaultString(error.getCode(), error.getClass().getSimpleName());
                    var codeError = UPPER_CAMEL.to(LOWER_UNDERSCORE, code);
                    return new ErrorInfo(codeError, field, error.getDefaultMessage(), null);
                }).findFirst().orElseGet(() -> new ErrorInfo("generic"));
    }
}
