package tech.introduct.mailbox.hadoop;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.persistence.domain.FileEntity;
import tech.introduct.mailbox.persistence.repository.FileRepository;
import tech.introduct.mailbox.web.handler.ErrorInfoRuntimeException;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_PLAIN;

class HadoopFileStorageTest {
    private FileSystem fileSystem;
    private FileRepository repository;
    private HadoopFileStorage fileStorage;

    @BeforeEach
    void setUp() {
        var properties = new HadoopProperties();
        fileSystem = mock(FileSystem.class);
        repository = mock(FileRepository.class);
        fileStorage = new HadoopFileStorage(properties, fileSystem, repository);
    }

    @Test
    void whenSaveAndFileSystemUnavailable() throws Exception {
        var fileDto = new LoadedFileDto(UUID.randomUUID(), "test.txt", TEXT_PLAIN, "test".getBytes());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileSystem.create(any())).thenThrow(IOException.class);
        assertThrows(ErrorInfoRuntimeException.class, () -> fileStorage.save(fileDto));
    }

    @Test
    void whenLoadAndFileSystemUnavailable() throws Exception {
        var file = new FileEntity("test.txt", TEXT_PLAIN, "test.txt");
        when(fileSystem.open(any(Path.class))).thenThrow(IOException.class);
        assertThrows(ErrorInfoRuntimeException.class, () -> fileStorage.load(file));
    }
}
