package tech.introduct.mailbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class AuthTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void whenGetAdminToken_ThenOk() throws Exception {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("grant_type", "client_credentials");
        params.add("scope", "any");

        mvc.perform(post("/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .params(params)
                .with(httpBasic("admin-client", "password"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type", is("bearer")))
                .andExpect(jsonPath("$.scope", is("any")))
                .andExpect(jsonPath("$.expires_in", is(notNullValue())))
                .andExpect(jsonPath("$.access_token", is(notNullValue())));
    }

    @Test
    void whenGetAdminTokenWithWrongCredentials_ThenForbidden() throws Exception {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("grant_type", "client_credentials");
        params.add("scope", "any");

        mvc.perform(post("/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .params(params)
                .with(httpBasic("admin-client", "wrong"))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenClientStartAuthorization_ThenRedirectToTara() throws Exception {
        mvc.perform(get("/oauth2/authorization/tara"))
                .andExpect(status().isFound());
    }
}
