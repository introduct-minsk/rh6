package tech.introduct.mailbox.service.impl;

import org.digidoc4j.DataFile;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.file.LoadedFileDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleMessageDataFileConverterTest {
    private SimpleMessageDataFileConverter converter = new SimpleMessageDataFileConverter();

    @Test
    void convertFullMessage() {
        var expect = "From: <Sender First Name> <Sender Last Name> (<Sender identification code>)\r\n" +
                "To: <Recipient First Name> <Recipient Last Name> (<Recipient identification code>)\r\n" +
                "Subject <Message subject>\r\n" +
                "\r\n" +
                "<Message body>\r\n" +
                "\r\n" +
                "Attachments: <filename 1>\r\n" +
                "             <filename 2>";
        var files = List.of(
                new LoadedFileDto(UUID.randomUUID(), "<filename 1>", MediaType.TEXT_PLAIN, new byte[]{}),
                new LoadedFileDto(UUID.randomUUID(), "<filename 2>", MediaType.TEXT_PLAIN, new byte[]{})
        );
        var dataFiles = converter.convert(MessageDto.builder()
                .sender(new UserDto(
                        "<Sender identification code>",
                        "<Sender First Name>",
                        "<Sender Last Name>",
                        null))
                .receiver(new UserDto(
                        "<Recipient identification code>",
                        "<Recipient First Name>",
                        "<Recipient Last Name>",
                        null))
                .subject("<Message subject>")
                .text("<Message body>")
                .build(), files);
        assertNotNull(dataFiles);
        assertEquals(3, dataFiles.size());
        var dataFile = findMessageTxt(dataFiles);
        assertEquals(expect, new String(dataFile.getBytes()));
        assertEquals("<Message body>", converter.getMessageBody(List.of(dataFile)));
    }

    @Test
    void convertMessageWithoutAttachments() {
        var expect = "From: <Sender First Name> <Sender Last Name> (<Sender identification code>)\r\n" +
                "To: <Recipient First Name> <Recipient Last Name> (<Recipient identification code>)\r\n" +
                "Subject <Message subject>\r\n" +
                "\r\n" +
                "<Message body>";
        var dataFiles = converter.convert(MessageDto.builder()
                .sender(new UserDto(
                        "<Sender identification code>",
                        "<Sender First Name>",
                        "<Sender Last Name>",
                        null))
                .receiver(new UserDto(
                        "<Recipient identification code>",
                        "<Recipient First Name>",
                        "<Recipient Last Name>",
                        null))
                .subject("<Message subject>")
                .text("<Message body>")
                .build(), List.of());
        assertNotNull(dataFiles);
        assertEquals(1, dataFiles.size());
        var dataFile = findMessageTxt(dataFiles);
        assertEquals(expect, new String(dataFile.getBytes()));
        assertEquals("<Message body>", converter.getMessageBody(List.of(dataFile)));
    }

    @Test
    void convertMessageWithoutSubjectAndBody() {
        var expect = "From: <Sender First Name> <Sender Last Name> (<Sender identification code>)\r\n" +
                "To: <Recipient First Name> <Recipient Last Name> (<Recipient identification code>)\r\n" +
                "Subject";
        var dataFiles = converter.convert(MessageDto.builder()
                .sender(new UserDto(
                        "<Sender identification code>",
                        "<Sender First Name>",
                        "<Sender Last Name>",
                        null))
                .receiver(new UserDto(
                        "<Recipient identification code>",
                        "<Recipient First Name>",
                        "<Recipient Last Name>",
                        null))
                .build(), List.of());
        assertNotNull(dataFiles);
        assertEquals(1, dataFiles.size());
        var dataFile = findMessageTxt(dataFiles);
        assertEquals(expect, new String(dataFile.getBytes()));
        assertNull(converter.getMessageBody(List.of(dataFile)));
    }

    @Test
    void convertMessageWithOnBehalfOf() {
        var expect = "From: <Sender First Name> <Sender Last Name> (<Sender identification code>) on behalf of (<Identification code>)\r\n" +
                "To: <Recipient First Name> <Recipient Last Name> (<Recipient identification code>)\r\n" +
                "Subject";
        var dataFiles = converter.convert(MessageDto.builder()
                .sender(new UserDto(
                        "<Sender identification code>",
                        "<Sender First Name>",
                        "<Sender Last Name>",
                        "<Identification code>"))
                .receiver(new UserDto(
                        "<Recipient identification code>",
                        "<Recipient First Name>",
                        "<Recipient Last Name>",
                        null))
                .build(), List.of());
        assertNotNull(dataFiles);
        assertEquals(1, dataFiles.size());
        var dataFile = findMessageTxt(dataFiles);
        assertEquals(expect, new String(dataFile.getBytes()));
        assertNull(converter.getMessageBody(List.of(dataFile)));
    }

    @Test
    void convertFullMessageWithoutBody() {
        var expect = "From: <Sender First Name> <Sender Last Name> (<Sender identification code>)\r\n" +
                "To: <Recipient First Name> <Recipient Last Name> (<Recipient identification code>)\r\n" +
                "Subject <Message subject>";
        var dataFiles = converter.convert(MessageDto.builder()
                .sender(new UserDto(
                        "<Sender identification code>",
                        "<Sender First Name>",
                        "<Sender Last Name>",
                        null))
                .receiver(new UserDto(
                        "<Recipient identification code>",
                        "<Recipient First Name>",
                        "<Recipient Last Name>",
                        null))
                .subject("<Message subject>")
                .build(), Set.of());
        assertNotNull(dataFiles);
        assertEquals(1, dataFiles.size());
        var dataFile = findMessageTxt(dataFiles);
        assertEquals(expect, new String(dataFile.getBytes()));
        assertNull(converter.getMessageBody(List.of(dataFile)));
    }


    @Test
    void convertMessageWithoutReceiver() {
        var expect = "From: <Sender First Name> <Sender Last Name> (<Sender identification code>)\r\n" +
                "To: (<Recipient identification code>)\r\n" +
                "Subject <Message subject>";
        var dataFiles = converter.convert(MessageDto.builder()
                .sender(new UserDto(
                        "<Sender identification code>",
                        "<Sender First Name>",
                        "<Sender Last Name>",
                        null))
                .receiver(new UserDto(
                        "<Recipient identification code>",
                        null,
                        null,
                        null))
                .subject("<Message subject>")
                .build(), Set.of());
        assertNotNull(dataFiles);
        assertEquals(1, dataFiles.size());
        var dataFile = findMessageTxt(dataFiles);
        assertEquals(expect, new String(dataFile.getBytes()));
        assertNull(converter.getMessageBody(List.of(dataFile)));
    }

    @Test
    void getMessageBodyWithoufMessageTxt_ExpectException() {
        var dataFile = mock(DataFile.class);
        when(dataFile.getName()).thenReturn("not-message.txt");
        assertThrows(IllegalArgumentException.class, () -> converter.getMessageBody(List.of(dataFile)));
    }

    @Test
    void givenDataFilesEmpty_whenGetMessage_thenExceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> converter.getMessageBody(List.of()));
    }

    private DataFile findMessageTxt(Collection<DataFile> dataFiles) {
        return dataFiles.stream()
                .filter(file -> file.getName().equals("message.txt"))
                .findAny()
                .orElseThrow(AssertionError::new);
    }
}
