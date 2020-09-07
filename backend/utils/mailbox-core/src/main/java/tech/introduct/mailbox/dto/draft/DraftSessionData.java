package tech.introduct.mailbox.dto.draft;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.io.Serializable;
import java.util.UUID;

@Component
@SessionScope
public class DraftSessionData implements Serializable {
    private DraftMessage draftMessage = new DraftMessage();

    public DraftMessage create(DraftMessage value) {
        if (value != null) {
            draftMessage = value;
        }
        return value;
    }

    public void clear() {
        draftMessage = new DraftMessage();
    }

    public DraftMessage getMessage() {
        return draftMessage;
    }

    public LoadedFileDto getAttachment(UUID attachmentId) {
        return getMessage().getAttachments().stream()
                .filter(attachment -> attachment.getId().equals(attachmentId))
                .findAny()
                .orElseThrow(() -> new ErrorInfo("not_found", "attachmentId").badRequest());
    }
}
