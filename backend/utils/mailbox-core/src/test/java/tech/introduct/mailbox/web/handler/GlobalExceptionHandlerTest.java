package tech.introduct.mailbox.web.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAllException() {
        var entity = handler.handleAllException(new NullPointerException(), mock(WebRequest.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        entity = handler.handleAllException(new ErrorInfo("").badRequest());
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        entity = handler.handleExceptionInternal(new Exception(), new Object(), new HttpHeaders(),
                null, mock(WebRequest.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        entity = handler.handleExceptionInternal(new Exception(), new Object(), new HttpHeaders(),
                HttpStatus.SERVICE_UNAVAILABLE, mock(WebRequest.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        entity = handler.handleExceptionInternal(new Exception(), new Object(), new HttpHeaders(),
                HttpStatus.UNAUTHORIZED, mock(WebRequest.class));
        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
    }

    @Test
    void handleBadRequest() {
        var entity = handler.handleBadRequest(new MaxUploadSizeExceededException(1000), mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    void handleBindException() {
        var entity = handler.handleBindException(new BindException(new Object(), "name"), new HttpHeaders(),
                HttpStatus.BAD_REQUEST, mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }
}
