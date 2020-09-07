package tech.introduct.mailbox.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tech.introduct.mailbox.dto.MessageType;
import tech.introduct.mailbox.dto.NotificationType;
import tech.introduct.mailbox.persistence.domain.MessageEntity;
import tech.introduct.mailbox.persistence.repository.MessageRepository;

import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tech.introduct.mailbox.OAuth2AuthenticationHelper.bearerAdmin;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Transactional
class AdminMailboxControllerTest {
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected MessageRepository messageRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void find() throws Exception {
        var message = MessageEntity.builder()
                .type(MessageType.SIMPLE)
                .senderUserId(randomAlphanumeric(5))
                .sender(randomAlphanumeric(5))
                .receiver(randomAlphanumeric(5))
                .subject(randomAlphanumeric(10))
                .text(randomAlphanumeric(20))
                .build();

        var saved = messageRepository.save(message);

        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/admin/data/mailbox_db/messages/{id}", message.getId())
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().toString())))
                .andExpect(jsonPath("$.type", is(saved.getType().name())))
                .andExpect(jsonPath("$.subject", is(saved.getSubject())))
                .andExpect(jsonPath("$.sender", is(saved.getSender())))
                .andExpect(jsonPath("$.senderUserId", is(saved.getSenderUserId())))
                .andExpect(jsonPath("$.receiver", is(saved.getReceiver())))
                .andExpect(jsonPath("$.body.id", is(saved.getBody().getId().toString())))
                .andExpect(jsonPath("$.body.text", is(saved.getBody().getText())));
        mvc.perform(get("/admin/data/mailbox_db/messages")
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(saved.getId().toString())))
                .andExpect(jsonPath("$.content[0].type", is(saved.getType().name())))
                .andExpect(jsonPath("$.content[0].subject", is(saved.getSubject())))
                .andExpect(jsonPath("$.content[0].sender", is(saved.getSender())))
                .andExpect(jsonPath("$.content[0].senderUserId", is(saved.getSenderUserId())))
                .andExpect(jsonPath("$.content[0].receiver", is(saved.getReceiver())))
                .andExpect(jsonPath("$.content[0].body.id", is(saved.getBody().getId().toString())))
                .andExpect(jsonPath("$.content[0].body.text", is(saved.getBody().getText())));

        var notification = messageRepository.save(
                MessageEntity.toNotification(NotificationType.READ, message, randomAlphanumeric(5)));
        mvc.perform(get("/admin/data/mailbox_db/messages/{id}", notification.getId())
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notification.getId().toString())));
    }

    @Test
    void loadFileWithWrongId() throws Exception {
        mvc.perform(get("/admin/data/mailbox_db/files/{fileId}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("file_not_found")));
    }

    @Test
    void save() throws Exception {
        var fileContent = randomAlphanumeric(10).getBytes();
        var file = new MockMultipartFile("file", "text.txt", TEXT_PLAIN_VALUE, fileContent);
        byte[] response = mvc.perform(multipart("/admin/data/mailbox_db/files/upload")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(file.getOriginalFilename())))
                .andExpect(jsonPath("$.type", is(file.getContentType())))
                .andExpect(jsonPath("$.externalId").isString())
                .andReturn().getResponse().getContentAsByteArray();
        var fileId = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        var loadedFileContent = mvc.perform(get("/admin/data/mailbox_db/files/{fileId}", fileId)
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TEXT_PLAIN_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"text.txt\""))
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(fileContent, loadedFileContent);

        var message = MessageEntity.builder()
                .type(MessageType.SIMPLE)
                .senderUserId(randomNumeric(5))
                .sender(randomNumeric(5))
                .receiver(randomNumeric(5))
                .subject(randomAlphanumeric(10))
                .text(randomAlphanumeric(20))
                .build();
        message.setUnread(false);
        var createdOn = ZonedDateTime.now().minusYears(1);
        mvc.perform(post("/admin/data/mailbox_db/messages")
                .header(HttpHeaders.AUTHORIZATION, bearerAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("" +
                        "{" +
                        "  \"type\": \"" + message.getType() + "\"," +
                        "  \"subject\": \"" + message.getSubject() + "\"," +
                        "  \"createdOn\": \"" + createdOn.toString() + "\"," +
                        "  \"senderUserId\": \"" + message.getSenderUserId() + "\"," +
                        "  \"sender\": \"" + message.getSender() + "\"," +
                        "  \"receiver\": \"" + message.getReceiver() + "\"," +
                        "  \"unread\": \"" + message.isUnread() + "\"," +
                        "  \"attachments\": [" +
                        "    {" +
                        "      \"id\": \"" + fileId + "\"" +
                        "    }" +
                        "  ]," +
                        "  \"sign\": {" +
                        "    \"id\": \"" + fileId + "\"" +
                        "  }," +
                        "  \"body\": {" +
                        "    \"text\": \"" + message.getBody().getText() + "\"" +
                        "  }" +
                        "}" +
                        ""))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        var messages = messageRepository.findBySenderAndReceiver(message.getSender(), message.getReceiver());
        assertEquals(1, messages.size());
        var saved = messages.get(0);

        assertEquals(message.getType(), saved.getType());
        assertEquals(message.getSubject(), saved.getSubject());
        assertEquals(createdOn.toInstant(), saved.getCreatedOn().toInstant());
        assertEquals(message.getSenderUserId(), saved.getSenderUserId());
        assertEquals(message.getSender(), saved.getSender());
        assertEquals(message.getReceiver(), saved.getReceiver());
        assertEquals(1, saved.getAttachments().size());
        assertEquals(fileId, saved.getAttachments().iterator().next().getId());
        assertNull(message.getSign());
        assertEquals(message.getBody().getText(), saved.getBody().getText());
        assertEquals(message.isUnread(), saved.isUnread());
    }

    @Test
    void deny() throws Exception {
        mvc.perform(get("/admin/data/mailbox_db/messages")
                .header(HttpHeaders.AUTHORIZATION, "basic"))
                .andExpect(status().isUnauthorized());
    }
}
