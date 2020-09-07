package tech.introduct.mailbox.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.digidoc4j.DataFile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.service.MessageDataFileConverter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static tech.introduct.mailbox.utils.EstonianIdUtils.addEEIfValid;

@Component
public class SimpleMessageDataFileConverter implements MessageDataFileConverter {
    private static final String FILE_NAME = "message.txt";
    private static final String SUBJECT = "Subject ";
    private static final String BODY_DIVIDER = IOUtils.LINE_SEPARATOR_WINDOWS + IOUtils.LINE_SEPARATOR_WINDOWS;
    private static final String ATTACHMENTS = "Attachments: ";

    @Override
    public Collection<DataFile> convert(MessageDto message, Collection<LoadedFileDto> attachments) {
        var txtBuilder = new StringBuilder()
                .append("From: ")
                .append(message.getSender().getFirstName())
                .append(" ")
                .append(message.getSender().getLastName())
                .append(" (")
                .append(message.getSender().getId())
                .append(")");
        if (message.getSender().isOnBehalfOf()) {
            txtBuilder
                    .append(" on behalf of (")
                    .append(message.getSender().getRoleId())
                    .append(")");
        }
        txtBuilder.append(IOUtils.LINE_SEPARATOR_WINDOWS).append("To: ");
        if (message.getReceiver().getFirstName() != null) {
            txtBuilder.append(message.getReceiver().getFirstName()).append(" ");
        }
        if (message.getReceiver().getLastName() != null) {
            txtBuilder.append(message.getReceiver().getLastName()).append(" ");
        }
        txtBuilder
                .append("(")
                .append(addEEIfValid(message.getReceiver().getId()))
                .append(")")
                .append(IOUtils.LINE_SEPARATOR_WINDOWS)
                .append(SUBJECT)
                .append(trimToEmpty(message.getSubject()))
                .append(BODY_DIVIDER)
                .append(trimToEmpty(message.getText()))
                .append(BODY_DIVIDER);
        if (!attachments.isEmpty()) {
            txtBuilder.append(ATTACHMENTS);
            attachments.forEach(fileDto -> txtBuilder.append(fileDto.getName())
                    .append(IOUtils.LINE_SEPARATOR_WINDOWS)
                    .append("             "));
        }
        var messageTxt = txtBuilder.toString().trim();
        return renameSameNameAttachments(attachments, messageTxt);
    }

    private Collection<DataFile> renameSameNameAttachments(Collection<LoadedFileDto> attachments, String messageTxt) {
        var names = new LinkedList<String>();
        names.add(FILE_NAME);
        var dataFiles = new LinkedList<DataFile>();
        dataFiles.add(new DataFile(messageTxt.getBytes(), FILE_NAME, MediaType.TEXT_PLAIN_VALUE));
        attachments.forEach(fileDto -> {
            var name = fileDto.getName();
            if (FILE_NAME.equals(name)) {
                name = "attachment_message.txt";
            }
            if (names.contains(name)) {
                var formatIndex = name.lastIndexOf('.');
                name = name.substring(0, formatIndex) + "_" + randomNumeric(5) + name.substring(formatIndex);
            }
            names.add(name);
            dataFiles.add(new DataFile(fileDto.getBytes(), name, fileDto.getType().toString()));
        });
        return Collections.unmodifiableCollection(dataFiles);
    }

    @Nullable
    @Override
    public String getMessageBody(Collection<DataFile> dataFiles) {
        var dataFile = dataFiles.stream()
                .filter(file -> FILE_NAME.equals(file.getName()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("dataFiles not contains message file"));
        var message = new String(dataFile.getBytes());
        var subjectIndex = message.indexOf(SUBJECT);
        var bodyIndex = message.indexOf(BODY_DIVIDER, subjectIndex);
        if (bodyIndex == -1) {
            return null;
        }
        var attachmentsIndex = message.indexOf(ATTACHMENTS, bodyIndex);
        var body = attachmentsIndex == -1 ? message.substring(bodyIndex) : message.substring(bodyIndex, attachmentsIndex);
        return StringUtils.trimToNull(body);
    }
}
