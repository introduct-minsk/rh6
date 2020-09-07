package tech.introduct.mailbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.introduct.mailbox.persistence.domain.RoleEntity;
import tech.introduct.mailbox.persistence.repository.ClientDetailsRepository;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService implements ClientDetailsService {
    private final ClientDetailsRepository repository;

    @Override
    @Transactional
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        try {
            return getClientDetails(clientId);
        } catch (Exception e) {
            log.error("loadClientByClientId", e);
            throw e;
        }
    }

    private ClientDetails getClientDetails(String clientId) {
        var client = repository.findByClientId(clientId).orElseThrow(() -> new NoSuchClientException(clientId));
        var details = new BaseClientDetails();
        details.setClientId(clientId);
        details.setClientSecret(client.getSecret());
        details.setAuthorizedGrantTypes(List.of("client_credentials"));
        details.setAuthorities(client.getRoles().stream()
                .map(RoleEntity::getRole)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        details.setAccessTokenValiditySeconds((int) Duration.ofHours(24).toSeconds());
        log.debug("get client details for id = {} {}", clientId, details);
        return details;
    }
}
