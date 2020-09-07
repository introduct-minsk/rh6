package tech.introduct.mailbox.hadoop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.persistence.domain.FileEntity;
import tech.introduct.mailbox.persistence.repository.FileRepository;
import tech.introduct.mailbox.service.FileStorage;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HadoopFileStorage implements FileStorage {
    private final HadoopProperties properties;
    private final FileSystem fileSystem;
    private final FileRepository repository;

    @Override
    public FileEntity save(LoadedFileDto fileDto) {
        var originalFileName = fileDto.getName();
        var fileFormat = originalFileName.substring(originalFileName.lastIndexOf('.'));
        var fileName = UUID.randomUUID() + fileFormat;
        Path writePath = new Path(properties.getPath() + "/" + fileName);
        FileEntity file = repository.save(new FileEntity(originalFileName, fileDto.getType(), writePath.toString()));
        try {
            FSDataOutputStream outputStream = fileSystem.create(writePath);
            outputStream.write(fileDto.getBytes());
            outputStream.close();
        } catch (Exception e) {
            log.error("hadoop save file {}", writePath, e);
            throw new ErrorInfo("file_storage_unavailable").unavailable();
        }
        log.debug("Hadooop file {} was saved", writePath);
        return file;
    }

    @Override
    public LoadedFileDto load(FileEntity file) {
        Path readPath = new Path(file.getExternalId());
        try {
            var open = fileSystem.open(readPath);
            log.debug("Hadooop loaded {}", readPath);
            return new LoadedFileDto(file.getId(), file.getName(), file.getType(), IOUtils.toByteArray(open));
        } catch (Exception e) {
            log.error("hadoop load file {}", readPath, e);
            throw new ErrorInfo("file_storage_unavailable").unavailable();
        }
    }
}
