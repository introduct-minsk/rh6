package tech.introduct.mailbox.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.introduct.mailbox.OAuth2AuthenticationHelper.bearerWithRole;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class PrivateUserControllerTest {
    @Autowired
    protected MockMvc mvc;

    @Test
    void whenFindForPrivateApi_ThenOk() throws Exception {
        var ids = Set.of("EE60001019906").toArray(String[]::new);

        mvc.perform(get("/private/api/users")
                .param("id", ids)
                .header(HttpHeaders.AUTHORIZATION, bearerWithRole("ROLE_GET_USER_DETAILS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("EE60001019906")))
                .andExpect(jsonPath("$[0].firstName", is("MARY ÄNN")))
                .andExpect(jsonPath("$[0].lastName", is("O'CONNEŽ-ŠUSLIK")))
                .andExpect(jsonPath("$[0].dateOfBirth").doesNotExist())
                .andExpect(jsonPath("$[0].address").doesNotExist())
                .andExpect(jsonPath("$[0].role").doesNotExist());
    }
}
