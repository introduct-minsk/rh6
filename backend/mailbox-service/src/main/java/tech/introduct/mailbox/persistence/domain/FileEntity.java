package tech.introduct.mailbox.persistence.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
import org.springframework.http.MediaType;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_info")
@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class FileEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NonNull
    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @NonNull
    @Column(name = "type", nullable = false, updatable = false)
    @Convert(converter = MediaTypeToStringConverter.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private MediaType type;

    @NonNull
    @Column(name = "external", nullable = false, updatable = false)
    private String externalId;


    @Converter
    static class MediaTypeToStringConverter implements AttributeConverter<MediaType, String> {

        @Override
        public String convertToDatabaseColumn(MediaType type) {
            return type == null ? null : type.toString();
        }

        @Override
        public MediaType convertToEntityAttribute(String type) {
            return type == null ? null : MediaType.parseMediaType(type);
        }
    }
}
