package tech.introduct.mailbox.xroad;

import org.junit.jupiter.api.Test;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import static org.mockito.Mockito.*;

class XRoadConfigTest {

    @Test
    void setTimeouts() {
        var xRoadConfig = new XRoadConfig();
        var properties = new XRoadProperties();
        var urlConnectionMessageSender = mock(HttpUrlConnectionMessageSender.class);
        xRoadConfig.setTimeouts(urlConnectionMessageSender, properties);
        verify(urlConnectionMessageSender).setConnectionTimeout(properties.getConnectionTimeout());
        verify(urlConnectionMessageSender).setReadTimeout(properties.getReadTimeout());
        var webServiceMessageSender = mock(WebServiceMessageSender.class);
        xRoadConfig.setTimeouts(webServiceMessageSender, properties);
        verifyNoInteractions(webServiceMessageSender);
    }
}