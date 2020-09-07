package tech.introduct.mailbox.dto.draft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.dto.sign.SignatureDto;
import tech.introduct.mailbox.dto.sign.SigningData;
import tech.introduct.mailbox.validation.OnlyDigits;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class DraftMessage implements Serializable {
    @Size(max = 11)
    @OnlyDigits
    private String receiver;
    @Size(max = 255)
    private String subject;
    @Size(max = 2048)
    private String text;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<LoadedFileDto> attachments = new LinkedList<>();
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SignatureDto signature;
    @JsonIgnore
    private SigningData signingData;
    @JsonIgnore
    private LoadedFileDto sign;

    public DraftMessage(String receiver, String subject, String text) {
        this.receiver = receiver;
        this.subject = subject;
        this.text = text;
    }

    @JsonIgnore
    public void removeSignDataIfNotSigned() {
        if (isSigned()) {
            throw new ErrorInfo("already_signed").badRequest();
        }
        signingData = null;
    }

    @JsonIgnore
    public boolean isSigned() {
        return sign != null;
    }

    public Optional<SigningData> getSigningData() {
        return Optional.ofNullable(signingData);
    }

    public Optional<LoadedFileDto> getSign() {
        return Optional.ofNullable(sign);
    }
}
