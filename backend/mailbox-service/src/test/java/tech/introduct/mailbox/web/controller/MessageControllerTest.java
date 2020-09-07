package tech.introduct.mailbox.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.apache.commons.io.FileUtils;
import org.digidoc4j.DataFile;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tech.introduct.mailbox.WithMockOAuth2User;
import tech.introduct.mailbox.client.SearchClient;
import tech.introduct.mailbox.client.UserClient;
import tech.introduct.mailbox.dto.MessageType;
import tech.introduct.mailbox.dto.draft.DraftSessionData;
import tech.introduct.mailbox.dto.sign.SignatureDto;
import tech.introduct.mailbox.dto.user.UserRole;
import tech.introduct.mailbox.dto.user.UserSessionData;
import tech.introduct.mailbox.persistence.domain.MessageEntity;
import tech.introduct.mailbox.persistence.repository.MessageRepository;
import tech.introduct.mailbox.service.SignValidator;
import tech.introduct.mailbox.utils.TestSigningData;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tech.introduct.mailbox.utils.MockUtils.configureMock;
import static tech.introduct.mailbox.utils.SessionUtils.getUserId;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageControllerTest {
    private static final String senderId = "EE39707170000";
    private static final String receiverId = "49505250108";
    private static final String subject = randomAlphabetic(5);
    private static final String notification_subject = "NOTIFICATION_FLOW" + randomAlphabetic(5);
    private static final String key = randomAlphabetic(5);
    private static final String text = randomAlphabetic(50) + " " + key;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private DraftSessionData sessionData;
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MockHttpSession session;
    @MockBean
    private UserSessionData userData;
    @MockBean
    private UserClient userClient;
    @MockBean
    private SearchClient searchClient;
    @MockBean
    private SignValidator signValidator;

    @BeforeEach
    void setUp() {
        configureMock(userData);
        configureMock(userClient);
        configureMock(searchClient);
    }

    @Test
    @WithMockOAuth2User(sub = senderId)
    @Order(1)
    void flowGetNotification_whenReadMessage_1_send() throws Exception {
        mvc.perform(get("/api/v1/messages/draft").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiver").isEmpty())
                .andExpect(jsonPath("$.subject").isEmpty())
                .andExpect(jsonPath("$.text").isEmpty())
                .andExpect(jsonPath("$.attachments").isEmpty());
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + notification_subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");
        userData.setCurrentRole(new UserRole(getUserId(session)));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        var id = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        mvc.perform(get("/api/v1/messages/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOAuth2User(sub = receiverId)
    @Order(2)
    void flowGetNotification_whenReadMessage_2_readMessage() throws Exception {
        userData.setCurrentRole(new UserRole("EE" + receiverId));
        var response = mvc.perform(get("/api/v1/messages?direction={direction}", "IN"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        var id = StreamSupport.stream(new ObjectMapper().readTree(response).get("content").spliterator(), false)
                .filter(node -> notification_subject.equals(node.get("subject").textValue()))
                .findAny()
                .map(node -> node.get("id").textValue())
                .orElseThrow(AssertionError::new);

        mvc.perform(get("/api/v1/messages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(text)));

        mvc.perform(get("/api/v1/messages/{id}", id))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/messages?direction={direction}", "OUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));

        when(searchClient.apiFindSourceIds(any(), any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int page = invocation.getArgument(2);
            int size = invocation.getArgument(3);
            return new SearchClient.UUIDPage(List.of(UUID.fromString(id)), page, size, 1L);
        });
        mvc.perform(get("/api/v1/messages/search?query={key}", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].subject", is(notification_subject)));
    }

    @Test
    @WithMockOAuth2User(sub = senderId)
    @Order(3)
    void flowGetNotification_whenReadMessage_3_readNotification() throws Exception {
        userData.setCurrentRole(new UserRole(senderId));
        var response = mvc.perform(get("/api/v1/messages?direction={direction}", "IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(notNullValue())))
                .andExpect(jsonPath("$.content[0].type", is("NOTIFICATION")))
                .andExpect(jsonPath("$.content[0].sender.id", is("EE" + receiverId)))
                .andExpect(jsonPath("$.content[0].receiver.id", is(senderId)))
                .andExpect(jsonPath("$.content[0].subject", is("READ")))
                .andExpect(jsonPath("$.content[0].createdOn", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();

        var id = StreamSupport.stream(new ObjectMapper().readTree(response).get("content").spliterator(), false)
                .findAny()
                .map(node -> node.get("id").textValue())
                .orElseThrow(AssertionError::new);

        mvc.perform(get("/api/v1/messages/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOAuth2User
    void flowSearchMessage_whenExceptions() throws Exception {
        userData.setCurrentRole(new UserRole("role"));

        mvc.perform(get("/api/v1/messages?direction={direction}", "OUT"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/messages"))
                .andExpect(status().isOk());

        doThrow(FeignException.BadRequest.class).when(searchClient).apiFindSourceIds(any(), any(), anyInt(), anyInt());
        mvc.perform(get("/api/v1/messages/search?query={key}", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        doThrow(RuntimeException.class).when(searchClient).apiFindSourceIds(any(), any(), anyInt(), anyInt());
        mvc.perform(get("/api/v1/messages/search?query={key}", key))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("search_service_unavailable")));
    }

    @Test
    @WithMockOAuth2User(sub = senderId)
    void whenSendAndUserClientUnavailable() throws Exception {
        when(userClient.apiGetUserDetails(anySet())).thenThrow(FeignException.BadGateway.class);
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + "1234" + "\"" +
                "}" +
                "");
        userData.setCurrentRole(new UserRole(getUserId(session)));
        mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("user_service_unavailable")));
    }

    @Test
    @WithMockOAuth2User
    void whenSendAndSearchClientUnavailable() throws Exception {
        doThrow(FeignException.BadGateway.class).when(searchClient).apiCreateSource(any());
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"" +
                "}" +
                "");
        userData.setCurrentRole(new UserRole(getUserId(session)));
        mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("search_service_unavailable")));
    }

    @Test
    @WithMockOAuth2User(sub = senderId)
    void whenSendSimpleMessage_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId.replace("EE", "") + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");
        userData.setCurrentRole(new UserRole(getUserId(session)));
        mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.type", is("SIMPLE")))
                .andExpect(jsonPath("$.sender.id", is(senderId)))
                .andExpect(jsonPath("$.sender.firstName", is("F" + senderId)))
                .andExpect(jsonPath("$.sender.lastName", is("L" + senderId)))
                .andExpect(jsonPath("$.receiver.id", is("EE" + receiverId)))
                .andExpect(jsonPath("$.receiver.firstName", is("F" + "EE" + receiverId)))
                .andExpect(jsonPath("$.receiver.lastName", is("L" + "EE" + receiverId)))
                .andExpect(jsonPath("$.subject", is(subject)))
                .andExpect(jsonPath("$.createdOn", is(notNullValue())));
    }

    @Test
    @WithMockOAuth2User(sub = senderId)
    void whenSendMessageWithOnBehalfOf_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"" +
                "}" +
                "");
        var roleId = randomNumeric(10);
        userData.setCurrentRole(new UserRole(roleId));
        mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.sender.id", is(senderId)))
                .andExpect(jsonPath("$.sender.roleId", is(roleId)))
                .andExpect(jsonPath("$.receiver.id", is("EE" + receiverId)));
    }

    @Test
    @WithMockOAuth2User
    void whenTrySendWithWrongReceiver_ThenBadRequest() throws Exception {
        createDraft("{}");
        mvc.perform(patch("/api/v1/messages/draft")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"receiver\": \"" + "wrong" + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("only_digits")))
                .andExpect(jsonPath("$.field", is("receiver")));
        mvc.perform(patch("/api/v1/messages/draft")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"receiver\": \"" + "12345678901234" + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("size")))
                .andExpect(jsonPath("$.field", is("receiver")));
    }

    @Test
    @WithMockOAuth2User
    void whenSendWithoutReceiver_ThenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");
        mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("empty")))
                .andExpect(jsonPath("$.field", is("receiver")));
    }

    @Test
    @WithMockOAuth2User
    void whenTryReedNotYoursMessage_ThenNotFound() throws Exception {
        userData.setCurrentRole(new UserRole(randomNumeric(11)));
        var saved = messageRepository.save(MessageEntity.builder()
                .type(MessageType.SIMPLE)
                .sender(randomNumeric(11))
                .senderUserId(randomNumeric(11))
                .receiver(randomNumeric(11))
                .build());
        mvc.perform(get("/api/v1/messages/{id}", saved.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("not_found")));
    }

    @Test
    @WithMockOAuth2User
    void whenSendMessageWithAttachment_thenOk() throws Exception {
        createDraft("{}");

        var file = randomFile("other.txt");
        UUID fileId = uploadAttachment(file);

        mvc.perform(get("/api/v1/messages/draft/attachments/{fileId}", fileId)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TEXT_PLAIN_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"other.txt\""));

        file = randomFile("other_2.txt");
        updateAttachment(fileId, file);

        mvc.perform(patch("/api/v1/messages/draft/")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"receiver\": \"" + "39401120214" + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk());

        userData.setCurrentRole(new UserRole(getUserId(session)));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();
        var id = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        mvc.perform(get("/api/v1/messages/{id}", id)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments[0].name", is(file.getOriginalFilename())));
    }

    @Test
    @WithMockOAuth2User
    void whenSendMessageWithSign_thenOk_expectBodyNotStoreInDB() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");
        SignatureDto signature = sign();
        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature.valid", is(signature.getValid())))
                .andExpect(jsonPath("$.signature.signedBy", is(signature.getSignedBy())));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();
        var id = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());
        assertNull(messageRepository.findById(id).orElseThrow().getBody());
        mvc.perform(get("/api/v1/messages/{id}", id)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature.valid", is(signature.getValid())))
                .andExpect(jsonPath("$.signature.signedBy", is(signature.getSignedBy())))
                .andExpect(jsonPath("$.text", is(text)));
    }

    @Test
    @WithMockOAuth2User
    void whenSendMessageWithSignAndAttachments_expectFilesInsideSign() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"" +
                "}" +
                "");
        uploadAttachment(randomFile());
        sign();
        byte[] response = mvc.perform(get("/api/v1/messages/draft/sign")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.etsi.asic-e+zip"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"message.asice\""))
                .andReturn().getResponse().getContentAsByteArray();
        FileUtils.writeByteArrayToFile(new File("target/message.asice"), response);
        assertNotEquals(0, response.length);
        var container = BDocContainerBuilder.aContainer()
                .fromStream(new ByteArrayInputStream(response))
                .build();
        var dataFiles = container.getDataFiles();
        assertEquals(2, dataFiles.size());
    }

    @Test
    @WithMockOAuth2User
    void whenSignMessage_afterChangeMessage_expectBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"" +
                "}" +
                "");
        sign();
        mvc.perform(patch("/api/v1/messages/draft")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"text\": \"" + text + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("already_signed")));
        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/v1/messages/draft")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOAuth2User
    void whenSignMessage_withSameNameAndMessageTxt_expectRenameFiles() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"" +
                "}" +
                "");
        uploadAttachment(randomFile("same_name.txt"));
        uploadAttachment(randomFile("same_name.txt"));
        uploadAttachment(randomFile("message.txt"));
        uploadAttachment(randomFile("message.txt"));
        sign();
        byte[] response = mvc.perform(get("/api/v1/messages/draft/sign").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        var container = BDocContainerBuilder.aContainer()
                .fromStream(new ByteArrayInputStream(response))
                .build();
        var dataFiles = container.getDataFiles();
        var names = dataFiles.stream()
                .map(DataFile::getName)
                .collect(Collectors.toSet());
        assertTrue(names.contains("message.txt"));
        assertTrue(names.contains("same_name.txt"));
        assertTrue(names.contains("attachment_message.txt"));
        assertEquals(5, names.size());
    }

    @Test
    @WithMockOAuth2User
    void whenCreateDraftMessage_uploadMoreThen25Attachments_expectBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"" +
                "}" +
                "");
        var file = randomFile();
        for (int i = 0; i < 25; i++) {
            uploadAttachment(file);
        }
        mvc.perform(multipart("/api/v1/messages/draft/attachments/upload")
                .file(file)
                .with(csrf())
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("max_attachments_limit_is_exceeded")));
    }

    @Test
    @WithMockOAuth2User
    void getDraftSettings_expectOk() throws Exception {
        mvc.perform(get("/api/v1/messages/draft/settings")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxAttachmentNumber", is(25)))
                .andExpect(jsonPath("$.maxFileSizeBytes", is(52428800)));
    }

    @Test
    @WithMockOAuth2User
    void givenDraftMessageSigned_whenDeleteSign_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");
        SignatureDto signature = sign();
        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature.valid", is(signature.getValid())))
                .andExpect(jsonPath("$.signature.signedBy", is(signature.getSignedBy())));

        mvc.perform(delete("/api/v1/messages/draft/sign")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature", is(nullValue())));
    }

    @Test
    @WithMockOAuth2User
    void givenReceiverEmpty_whenGetSignData_thenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        userData.setCurrentRole(new UserRole(getUserId(session)));
        String certInHex = TestSigningData.getRSASigningCertificateInHex();
        mvc.perform(get("/api/v1/messages/draft/sign/data")
                .session(session)
                .param("certInHex", certInHex))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("empty")))
                .andExpect(jsonPath("$.field", is("receiver")));
    }

    @Test
    @WithMockOAuth2User
    void givenUploadedTwoAttachment_whenDeleteAttachment_thenOk() throws Exception {
        int attachmentCount = 0;
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments", hasSize(attachmentCount++)));

        UUID firstAttachment = uploadAttachment(randomFile());

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments", hasSize(attachmentCount++)))
                .andExpect(jsonPath("$.attachments[*].id", contains(firstAttachment.toString())));

        UUID secondAttachment = uploadAttachment(randomFile());

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments", hasSize(attachmentCount--)))
                .andExpect(jsonPath("$.attachments[*].id",
                        containsInAnyOrder(is(firstAttachment.toString()), is(secondAttachment.toString()))));

        mvc.perform(delete("/api/v1/messages/draft/attachments/{attachmentId}", firstAttachment)
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstAttachment.toString())));

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments", hasSize(attachmentCount)))
                .andExpect(jsonPath("$.attachments[*].id", contains(secondAttachment.toString())))
                .andExpect(jsonPath("$.attachments[*].id", not(contains(firstAttachment.toString()))));
    }

    @Test
    @WithMockOAuth2User
    void givenDraftMessageNotSigned_whenLoadSign_thenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        mvc.perform(get("/api/v1/messages/draft/sign")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("not_signed")));
    }

    @Test
    @WithMockOAuth2User
    void givenSigningDataNotCreated_whenSign_thenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        String signatureInHex = TestSigningData.getRSASigningCertificateInHex();

        mvc.perform(post("/api/v1/messages/draft/sign")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"signatureInHex\": \"" + signatureInHex + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("not_sign_data")));
    }

    @Test
    @WithMockOAuth2User
    void givenDraftMessageCreated_whenUpdateSubject_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        String subject = randomAlphabetic(5);

        mvc.perform(patch("/api/v1/messages/draft")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"subject\": \"" + subject + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is(subject)));

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is(subject)));
    }

    @Test
    @WithMockOAuth2User
    void giveDraftMessageCreated_whenUpdateText_thenOK() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        String text = randomAlphabetic(50) + " " + key;

        mvc.perform(patch("/api/v1/messages/draft")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"text\": \"" + text + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(text)));

        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(text)));
    }

    @Test
    @WithMockOAuth2User
    void givenAttachmentUploaded_whenUpdate_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        uploadAttachment(randomFile());
        uploadAttachment(randomFile());
        uploadAttachment(randomFile());

        UUID fileId = uploadAttachment(randomFile());

        updateAttachment(fileId, randomFile());
    }

    @Test
    @WithMockOAuth2User
    void givenAttachmentNotUploaded_whenUpdate_thenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        mvc.perform(multipart("/api/v1/messages/draft/attachments/{id}", UUID.randomUUID())
                .file(randomFile()).with(request -> {
                    request.setMethod(HttpMethod.PUT.name());
                    return request;
                })
                .with(csrf())
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("not_found")))
                .andExpect(jsonPath("$.field", is("attachmentId")));
    }

    @Test
    @WithMockOAuth2User
    void givenMessageSignedAndSent_whenLoadSign_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");
        SignatureDto signature = sign();
        mvc.perform(get("/api/v1/messages/draft")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature.valid", is(signature.getValid())))
                .andExpect(jsonPath("$.signature.signedBy", is(signature.getSignedBy())));

        userData.setCurrentRole(new UserRole(getUserId(session)));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();

        UUID messageId = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        mvc.perform(get("/api/v1/messages/{messageId}/sign", messageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.etsi.asic-e+zip"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"message.asice\""));
    }

    @Test
    @WithMockOAuth2User
    void givenMessageNotSignedAndSent_whenLoadSign_thenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        userData.setCurrentRole(new UserRole(getUserId(session)));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();

        UUID messageId = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        mvc.perform(get("/api/v1/messages/{messageId}/sign", messageId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("message_not_signed")));
    }

    @Test
    @WithMockOAuth2User
    void givenMessageWithAttachmentSent_whenLoadAttachment_thenOk() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        uploadAttachment(randomFile("other.txt"));

        userData.setCurrentRole(new UserRole(getUserId(session)));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();

        UUID messageId = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        byte[] messageResponse = mvc.perform(get("/api/v1/messages/{messageId}", messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments", hasSize(greaterThan(0))))
                .andReturn().getResponse().getContentAsByteArray();

        UUID attachmentId = StreamSupport.stream(new ObjectMapper().readTree(messageResponse).get("attachments").spliterator(), false)
                .findAny()
                .map(node -> node.get("id").textValue())
                .map(UUID::fromString)
                .orElseThrow(AssertionError::new);

        mvc.perform(get("/api/v1/messages/{messageId}/attachments/{attachmentsId}", messageId, attachmentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TEXT_PLAIN_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"other.txt\""));
    }

    @Test
    @WithMockOAuth2User
    void givenMessageWithoutAttachmentSent_whenLoadAttachment_thenBadRequest() throws Exception {
        createDraft("" +
                "{" +
                "  \"receiver\": \"" + receiverId + "\"," +
                "  \"subject\": \"" + subject + "\"," +
                "  \"text\": \"" + text + "\"" +
                "}" +
                "");

        UUID attachmentId = UUID.randomUUID();

        userData.setCurrentRole(new UserRole(getUserId(session)));
        byte[] response = mvc.perform(post("/api/v1/messages/draft/send")
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andReturn().getResponse().getContentAsByteArray();

        UUID messageId = UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());

        mvc.perform(get("/api/v1/messages/{messageId}/attachments/{attachmentsId}", messageId, attachmentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("not_found")))
                .andExpect(jsonPath("$.field", is("attachmentId")));
    }

    private MockMultipartFile randomFile() {
        return randomFile(randomAlphabetic(10) + ".txt");
    }

    private MockMultipartFile randomFile(String filename) {
        return new MockMultipartFile("file", filename, TEXT_PLAIN_VALUE, randomAlphanumeric(10).getBytes());
    }

    private UUID uploadAttachment(MockMultipartFile file) throws Exception {
        byte[] response = mvc.perform(multipart("/api/v1/messages/draft/attachments/upload")
                .file(file)
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(file.getOriginalFilename())))
                .andReturn().getResponse().getContentAsByteArray();
        return UUID.fromString(new ObjectMapper().readTree(response).get("id").textValue());
    }

    private void updateAttachment(UUID fileId, MockMultipartFile file) throws Exception {
        mvc.perform(multipart("/api/v1/messages/draft/attachments/{id}", fileId)
                .file(file).with(request -> {
                    request.setMethod(HttpMethod.PUT.name());
                    return request;
                })
                .with(csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fileId.toString())))
                .andExpect(jsonPath("$.name", is(file.getOriginalFilename())));
    }

    private SignatureDto sign() throws Exception {
        userData.setCurrentRole(new UserRole(getUserId(session)));
        String certInHex = TestSigningData.getRSASigningCertificateInHex();
        mvc.perform(get("/api/v1/messages/draft/sign/data")
                .session(session)
                .param("certInHex", certInHex))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hex", is(notNullValue())));

        var signature = new SignatureDto(true, randomAlphanumeric(5), ZonedDateTime.now());
        when(signValidator.validate(any(), any())).thenReturn(signature);

        var dataToSign = sessionData.getMessage().getSigningData().orElseThrow().getDataToSign();
        String signatureInHex = TestSigningData.rsaSignData(dataToSign, DigestAlgorithm.SHA256);
        mvc.perform(post("/api/v1/messages/draft/sign")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"signatureInHex\": \"" + signatureInHex + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(signature.getValid())))
                .andExpect(jsonPath("$.signedBy", is(signature.getSignedBy())))
                .andExpect(jsonPath("$.signingTime", is(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(signature.getSigningTime()))));
        return signature;
    }

    private void createDraft(String json) throws Exception {
        mvc.perform(post("/api/v1/messages/draft")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}
