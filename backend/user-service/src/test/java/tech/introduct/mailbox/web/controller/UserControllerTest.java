package tech.introduct.mailbox.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tech.introduct.mailbox.WithMockOAuth2User;
import tech.introduct.mailbox.xroad.MockXRoadConsumer;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected MockXRoadConsumer xRoadConsumer;
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MockHttpSession session;

    @Test
    @WithMockOAuth2User(sub = "EE60001019906")
    void whenGetMeAndUpdateSettings_thenOk() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("EE60001019906")))
                .andExpect(jsonPath("$.firstName", is("MARY ÄNN")))
                .andExpect(jsonPath("$.lastName", is("O'CONNEŽ-ŠUSLIK")))
                .andExpect(jsonPath("$.dateOfBirth", is("01.01.2000")))
                .andExpect(jsonPath("$.address", is("Soome, Helsingi, Tyynenmerenkatu 14")))
                .andExpect(jsonPath("$.role.id", is("EE60001019906")));

        mvc.perform(get("/api/v1/users/me/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale", is("ee")));

        String locale = "en";
        mvc.perform(post("/api/v1/users/me/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("" +
                        "{" +
                        "  \"locale\": \"" + locale + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale", is(locale)));

        mvc.perform(get("/api/v1/users/me/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale", is(locale)));

        mvc.perform(post("/api/v1/users/me/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("" +
                        "{" +
                        "  \"locale\": \"" + "ee" + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOAuth2User(sub = "EE38306114712")
    void whenGetMe_thenNotFound() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("rr414_service_error_10027")))
                .andExpect(jsonPath("$.details", is("Isik puudub RRis")));
    }

    @Test
    @WithMockOAuth2User(sub = "EE60001019906")
    void whenGetEmptyRoles_thenOk() throws Exception {
        mvc.perform(get("/api/v1/users/me/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("EE60001019906")));
    }

    @Test
    @WithMockOAuth2User(sub = "EE51001091072")
    void whenGetAndUpdateRoles_thenOk() throws Exception {
        var currentRole = "70006317";

        mvc.perform(get("/api/v1/users/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").isEmpty());

        mvc.perform(get("/api/v1/users/me/roles").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder("EE51001091072", currentRole, "11430169")));

        mvc.perform(post("/api/v1/users/me/role")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"id\": \"" + currentRole + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(currentRole)));

        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").isEmpty());

        mvc.perform(get("/api/v1/users/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role.id").value(currentRole));
    }

    @Test
    @WithMockOAuth2User(sub = "EE51001091072")
    void whenUpdateRolesThatNotFound_thenNotAllowed() throws Exception {
        mvc.perform(get("/api/v1/users/me").session(session))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/users/me/role")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"id\": \"" + "not-role" + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("not_allowed")));
    }

    @Test
    @WithMockOAuth2User(sub = "EE32406210144")
    void whenGetMeAndRolesUnavailable_thenUnavailable() throws Exception {
        xRoadConsumer.setException(true);
        mvc.perform(get("/api/v1/users/me/roles").session(session))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("aar_oigused_service_unavailable")));
        xRoadConsumer.setException(false);

        xRoadConsumer.setUnavailable(true);
        mvc.perform(get("/api/v1/users/me").session(session))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("rr414_service_unavailable")));
        mvc.perform(get("/api/v1/users/me/roles").session(session))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error", is("aar_oigused_service_unavailable")));
        xRoadConsumer.setUnavailable(false);
    }

    @Test
    @WithMockOAuth2User(sub = "EE32406210345")
    void whenSetRole_ThatUnavailable_thenAllowedOnlyMyself() throws Exception {
        mvc.perform(get("/api/v1/users/me/roles")
                .session(session))
                .andExpect(status().isOk());
        xRoadConsumer.setUnavailable(true);
        mvc.perform(post("/api/v1/users/me/role")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("" +
                        "{" +
                        "  \"id\": \"" + "EE32406210345" + "\"" +
                        "}" +
                        ""))
                .andExpect(status().isOk());
        xRoadConsumer.setUnavailable(false);
    }
}
