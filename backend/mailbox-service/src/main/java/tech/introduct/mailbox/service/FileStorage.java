package tech.introduct.mailbox.service;

import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.persistence.domain.FileEntity;

public interface FileStorage {

    FileEntity save(LoadedFileDto fileDto);

    LoadedFileDto load(FileEntity file);
}
