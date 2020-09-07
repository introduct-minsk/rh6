package tech.introduct.mailbox.xroad.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Simple log interceptor that logs all web service call results at the DEBUG level.
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingClientInterceptor implements ClientInterceptor {
    private final String prefix;

    /**
     * X-Road SOAP request notifications.
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        return logMessage(messageContext.getRequest(), "request");
    }

    /**
     * X-Road SOAP response messages.
     */
    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return logMessage(messageContext.getResponse(), "response");
    }

    /**
     * X-Road SOAP error messages from standard SOAP error messages
     */
    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return logMessage(messageContext.getResponse(), "fault");
    }

    @Override
    public void afterCompletion(MessageContext arg0, Exception arg1) throws WebServiceClientException {
    }

    private boolean logMessage(WebServiceMessage message, String type) {
        if (log.isDebugEnabled() && message instanceof SaajSoapMessage) {
            try (OutputStream out = new ByteArrayOutputStream()) {
                message.writeTo(out);
                log.debug("{} {}: {}", prefix, type, out.toString());
            } catch (Exception e) {
                log.warn("when logs", e);
            }
        }
        return true;
    }
}
