package tech.introduct.mailbox.persistence.domain;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileEntityTest {

    @Test
    void mediaTypeToStringConverterTest() {
        var converter = new FileEntity.MediaTypeToStringConverter();
        var mediaType = MediaType.APPLICATION_JSON;
        var converted = converter.convertToDatabaseColumn(mediaType);
        assertEquals(mediaType, converter.convertToEntityAttribute(converted));
    }

    @Test
    void nullMediaTypeToStringConverterTest() {
        var converter = new FileEntity.MediaTypeToStringConverter();
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }
}
