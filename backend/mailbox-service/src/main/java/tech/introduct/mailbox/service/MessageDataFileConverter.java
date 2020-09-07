package tech.introduct.mailbox.service;

import org.digidoc4j.DataFile;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.file.LoadedFileDto;

import javax.annotation.Nullable;
import java.util.Collection;

public interface MessageDataFileConverter {

    Collection<DataFile> convert(MessageDto message, Collection<LoadedFileDto> attachments);

    @Nullable
    String getMessageBody(Collection<DataFile> dataFiles);
}
