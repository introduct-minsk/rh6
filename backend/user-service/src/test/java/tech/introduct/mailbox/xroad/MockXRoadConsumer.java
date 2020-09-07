package tech.introduct.mailbox.xroad;

import com.nortal.jroad.client.exception.XRoadServiceConsumptionException;
import com.nortal.jroad.client.service.callback.CustomCallback;
import com.nortal.jroad.client.service.configuration.XRoadServiceConfiguration;
import com.nortal.jroad.client.service.consumer.StandardXRoadConsumer;
import com.nortal.jroad.client.service.consumer.XRoadConsumer;
import com.nortal.jroad.client.service.extractor.CustomExtractor;
import com.nortal.jroad.model.XRoadMessage;
import ee.riik.xtee.client.types.ee.riik.xtee.aar.producers.producer.aar.OigusedDocument;
import ee.riik.xtee.client.types.eu.x_road.rr.producer.RR414Document;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.ws.test.client.RequestMatchers;
import org.springframework.ws.test.client.ResponseCreators;

import java.util.Objects;

@Primary
@Component
@ConditionalOnProperty(value = "x-road.mock", havingValue = "true")
@RequiredArgsConstructor
public class MockXRoadConsumer implements XRoadConsumer {
    private final StandardXRoadConsumer xRoadConsumer;
    @Setter
    private boolean unavailable;
    @Setter
    private boolean exception;

    @Override
    public <I, O> XRoadMessage<O> sendRequest(XRoadMessage<I> input, XRoadServiceConfiguration configuration)
            throws XRoadServiceConsumptionException {
        mockServer(input);
        return xRoadConsumer.sendRequest(input, configuration);
    }

    @Override
    public <I, O> XRoadMessage<O> sendRequest(XRoadMessage<I> input, XRoadServiceConfiguration configuration,
                                              CustomCallback callback, CustomExtractor extractor)
            throws XRoadServiceConsumptionException {
        mockServer(input);
        return xRoadConsumer.sendRequest(input, configuration, callback, extractor);
    }

    @SneakyThrows
    public <I> void mockServer(XRoadMessage<I> input) {
        if (exception) {
            throw new Exception();
        }
        var server = MockWebServiceServer.createServer(xRoadConsumer);
        if (unavailable) {
            server.expect(RequestMatchers.anything())
                    .andRespond(ResponseCreators.withSoapEnvelope(new ClassPathResource("mock/unavailable.xml")));
            return;
        }
        Resource soapEnvelope = null;
        if (input.getContent() instanceof RR414Document.RR414) {
            soapEnvelope = new ClassPathResource("mock/rr414/not_found.xml");
            var id = ((RR414Document.RR414) input.getContent()).getRequest().getIsikukood();
            var customResource = new ClassPathResource("mock/rr414/" + id + ".xml");
            if (customResource.exists()) {
                soapEnvelope = customResource;
            }
        }
        if (input.getContent() instanceof OigusedDocument.Oigused) {
            soapEnvelope = new ClassPathResource("mock/aar/not_found.xml");
            var id = ((OigusedDocument.Oigused) input.getContent()).getKeha().getIsikukood();
            var customResource = new ClassPathResource("mock/aar/" + id + ".xml");
            if (customResource.exists()) {
                soapEnvelope = customResource;
            }
        }
        Objects.requireNonNull(soapEnvelope, "not set soapEnvelope for mock");
        server.expect(RequestMatchers.anything())
                .andRespond(ResponseCreators.withSoapEnvelope(soapEnvelope));
    }
}
