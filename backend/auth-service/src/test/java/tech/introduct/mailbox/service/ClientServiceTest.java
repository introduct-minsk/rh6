package tech.introduct.mailbox.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import tech.introduct.mailbox.persistence.domain.ClientDetailsEntity;
import tech.introduct.mailbox.persistence.domain.RoleEntity;
import tech.introduct.mailbox.persistence.repository.ClientDetailsRepository;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientServiceTest {
    private ClientDetailsRepository repository;
    private ClientService service;

    @BeforeEach
    void setUp() {
        repository = mock(ClientDetailsRepository.class);
        service = new ClientService(repository);
    }

    @Test
    void whenLoadClient_ThenOk() {
        var id = randomAlphanumeric(5);
        var secret = randomAlphanumeric(10);
        var role = randomAlphanumeric(5);

        var roleEntity = new RoleEntity(null, role, null);
        var clientDetailsEntity = new ClientDetailsEntity(id, secret, List.of(roleEntity));
        when(repository.findByClientId(eq(id))).thenReturn(Optional.of(clientDetailsEntity));

        var clientDetails = service.loadClientByClientId(id);

        assertEquals(id, clientDetails.getClientId());
        assertEquals(secret, clientDetails.getClientSecret());
        assertEquals(1, clientDetails.getAuthorities().size());
        assertEquals("ROLE_" + role, clientDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void whenLoadClient_ThenException() {

        var id = randomAlphanumeric(5);
        when(repository.findByClientId(eq(id))).thenReturn(Optional.empty());
        assertThrows(NoSuchClientException.class, () -> service.loadClientByClientId(id));
    }
}
