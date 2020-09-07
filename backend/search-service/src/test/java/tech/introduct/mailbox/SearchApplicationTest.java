package tech.introduct.mailbox;

import org.elasticsearch.ElasticsearchStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tech.introduct.mailbox.elasticsearch.domain.MailboxSource;
import tech.introduct.mailbox.elasticsearch.repository.SourceRepository;

import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.introduct.mailbox.OAuth2AuthenticationHelper.bearerWithRole;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class SearchApplicationTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SourceRepository sourceRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void whenSubmitSource_ThenSearch_ThenOk() throws Exception {
        var id = "6424e1ff-b417-4b24-acfb-dd3dbadd26e3";
        var sender = "EE60001019906";
        mvc.perform(post("/private/api/search/sources")
                .header(HttpHeaders.AUTHORIZATION, bearerWithRole("ROLE_SEARCH_SOURCE"))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("" +
                        "{" +
                        "  \"id\": \"" + id + "\"," +
                        "  \"sender\": \"" + sender + "\"," +
                        "  \"receiver\": \"EE39407120022\"," +
                        "  \"date\": \"" + ZonedDateTime.now() + "\"," +
                        "  \"value\": \"tester java tester\"" +
                        "}" +
                        ""))
                .andExpect(status().isCreated());

        var page = new PageImpl<>(List.of(id)).map(sourceId -> {
            var source = new MailboxSource();
            source.setId(sourceId);
            return source;
        });
        when(sourceRepository.search(any(), any())).thenReturn(page);

        search(sender)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]", is(id)));
    }

    @Test
    void whenElasticSearchNotAvailable_thenSearch_thenExceptionThrown() throws Exception {
        var sender = "EE60001019906";

        ElasticsearchStatusException exception = new ElasticsearchStatusException("Error", INTERNAL_SERVER_ERROR);

        when(sourceRepository.search(any(), any())).thenThrow(exception);

        search(sender)
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error", is("Error")));

    }

    private ResultActions search(String sender) throws Exception {
        return mvc.perform(get("/private/api/search/sources/id?role={role}&query={query}", sender, "java")
                .header(HttpHeaders.AUTHORIZATION, bearerWithRole("ROLE_SEARCH_SOURCE")));
    }
}
