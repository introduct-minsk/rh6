package tech.introduct.mailbox.web.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;

@JsonComponent
public class PageSerializer extends JsonSerializer<PageImpl<?>> {

    @Override
    public void serialize(PageImpl<?> page, JsonGenerator json, SerializerProvider provider) throws IOException {
        json.writeStartObject();
        json.writeObjectField("content", page.getContent());
        json.writeNumberField("totalPages", page.getTotalPages());
        json.writeNumberField("totalElements", page.getTotalElements());
        json.writeNumberField("size", page.getSize());
        json.writeNumberField("number", page.getNumber());
        json.writeEndObject();
    }
}
