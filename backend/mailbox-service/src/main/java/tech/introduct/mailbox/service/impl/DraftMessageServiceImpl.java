package tech.introduct.mailbox.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.introduct.mailbox.client.UserClient;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.draft.DraftMessage;
import tech.introduct.mailbox.dto.draft.DraftSessionData;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.dto.sign.SignatureDto;
import tech.introduct.mailbox.dto.sign.SigningData;
import tech.introduct.mailbox.dto.user.UserSessionData;
import tech.introduct.mailbox.properties.MailboxProperties;
import tech.introduct.mailbox.service.DraftMessageService;
import tech.introduct.mailbox.service.MessageDataFileConverter;
import tech.introduct.mailbox.service.SignValidator;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftMessageServiceImpl implements DraftMessageService {
    private final MailboxProperties mailboxProperties;
    private final DraftSessionData draftData;
    private final UserSessionData userData;
    private final MessageDataFileConverter dataFileConverter;
    private final UserClient userClient;
    private final FileSigner signer;
    private final SignValidator signValidator;

    @Override
    public DraftMessage create(DraftMessage request) {
        return draftData.create(request);
    }

    @Override
    public DraftMessage update(DraftMessage request) {
        var draftMessage = draftData.getMessage();
        draftMessage.removeSignDataIfNotSigned();
        if (request.getReceiver() != null) {
            draftMessage.setReceiver(request.getReceiver());
        }
        if (request.getSubject() != null) {
            draftMessage.setSubject(request.getSubject());
        }
        if (request.getText() != null) {
            draftMessage.setText(request.getText());
        }
        return draftMessage;
    }

    @Override
    public DraftMessage get() {
        return draftData.getMessage();
    }

    @Override
    public DraftMessage delete() {
        var message = draftData.getMessage();
        draftData.clear();
        return message;
    }

    @Override
    @SneakyThrows
    public LoadedFileDto uploadAttachment(MultipartFile multipart) {
        var attachment = new LoadedFileDto(multipart);
        var draftMessage = draftData.getMessage();
        draftMessage.removeSignDataIfNotSigned();
        var attachments = draftMessage.getAttachments();
        if (attachments.size() >= mailboxProperties.getMaxAttachmentNumber()) {
            throw new ErrorInfo("max_attachments_limit_is_exceeded").badRequest();
        }
        attachments.add(attachment);
        return attachment;
    }

    @Override
    @SneakyThrows
    public LoadedFileDto updateAttachment(UUID attachmentsId, MultipartFile multipart) {
        var attachment = new LoadedFileDto(attachmentsId, multipart);
        var draftMessage = draftData.getMessage();
        draftMessage.removeSignDataIfNotSigned();
        var currentAttachment = draftData.getAttachment(attachmentsId);
        draftMessage.getAttachments().replaceAll(fileDto -> {
            if (currentAttachment.equals(fileDto)) {
                return attachment;
            }
            return fileDto;
        });
        return attachment;
    }

    @Override
    public LoadedFileDto loadAttachment(UUID attachmentsId) {
        return draftData.getAttachment(attachmentsId);
    }

    @Override
    public LoadedFileDto deleteAttachment(UUID attachmentsId) {
        var attachment = draftData.getAttachment(attachmentsId);
        var draftMessage = draftData.getMessage();
        draftMessage.removeSignDataIfNotSigned();
        draftMessage.getAttachments().remove(attachment);
        return attachment;
    }

    @Override
    public String getDataToSignInHex(AuthenticatedPrincipal user, String certInHex) {
        var draftMessage = draftData.getMessage();
        var signingData = new SigningData(certInHex);
        if (StringUtils.isEmpty(draftMessage.getReceiver())) {
            throw new ErrorInfo("empty", "receiver").badRequest();
        }
        var roleId = userData.requiredCurrentRoleId();
        var userDetails = userClient.getUserDetailsMap(List.of(user.getName(), draftMessage.getReceiver()));
        var message = MessageDto.builder()
                .sender(userDetails.get(user.getName()).withRoleId(roleId))
                .receiver(userDetails.get(draftMessage.getReceiver()))
                .subject(draftMessage.getSubject())
                .text(draftMessage.getText())
                .build();
        var dataFiles = dataFileConverter.convert(message, draftMessage.getAttachments());
        signer.generateDataToSign(signingData, dataFiles);
        var hex = signingData.getDataToSignInHex();
        draftMessage.setSigningData(signingData);
        return hex;
    }

    @Override
    @SneakyThrows
    public SignatureDto sign(String signatureInHex) {
        var draftMessage = draftData.getMessage();
        var signingData = draftMessage.getSigningData()
                .orElseThrow(() -> new ErrorInfo("not_sign_data").badRequest());
        signingData.setSignatureInHex(signatureInHex);
        signer.signContainer(signingData);
        String fileName = "message." + signer.getFileFormat();
        var signBytes = IOUtils.toByteArray(signingData.getContainer().saveAsStream());
        draftMessage.setSign(new LoadedFileDto(UUID.randomUUID(), fileName, signer.getMimeType(), signBytes));
        var signature = signValidator.validate(fileName, signBytes);
        draftMessage.setSignature(signature);
        return signature;
    }

    @Override
    public void deleteSign() {
        var message = draftData.getMessage();
        message.setSigningData(null);
        message.setSignature(null);
        message.setSign(null);
    }

    @Override
    public LoadedFileDto loadSign() {
        return draftData.getMessage().getSign()
                .orElseThrow(() -> new ErrorInfo("not_signed").badRequest());
    }
}
