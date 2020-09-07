package tech.introduct.mailbox.dto.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.io.Serializable;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class FileDto implements Serializable {
    private final UUID id;
    private final String name;

    public FileDto(UUID id, String name) {
        if (StringUtils.isEmpty(name)) {
            throw new ErrorInfo("empty", "fileName").badRequest();
        }
        if (name.length() > 255) {
            throw new ErrorInfo("size", "fileName").badRequest();
        }
        this.id = id;
        this.name = name;
    }
}
