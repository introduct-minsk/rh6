package tech.introduct.mailbox.service;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.web.multipart.MultipartFile;
import tech.introduct.mailbox.dto.draft.DraftMessage;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.dto.sign.SignatureDto;

import java.util.UUID;

public interface DraftMessageService {

    DraftMessage create(DraftMessage request);

    DraftMessage update(DraftMessage request);

    DraftMessage get();

    DraftMessage delete();

    LoadedFileDto uploadAttachment(MultipartFile multipart);

    LoadedFileDto updateAttachment(UUID attachmentsId, MultipartFile multipart);

    LoadedFileDto loadAttachment(UUID attachmentsId);

    LoadedFileDto deleteAttachment(UUID attachmentsId);

    String getDataToSignInHex(AuthenticatedPrincipal user, String certInHex);

    SignatureDto sign(String signatureInHex);

    void deleteSign();

    LoadedFileDto loadSign();
}
