package tech.introduct.mailbox.xroad.impl;

import com.nortal.jroad.client.exception.XRoadServiceConsumptionException;
import ee.riik.xtee.client.database.AarXRoadDatabase;
import ee.riik.xtee.client.database.RrXRoadDatabase;
import ee.riik.xtee.client.types.ee.riik.xtee.aar.producers.producer.aar.OigusedDocument.Oigused;
import ee.riik.xtee.client.types.ee.riik.xtee.aar.producers.producer.aar.OigusedParing;
import ee.riik.xtee.client.types.ee.riik.xtee.aar.producers.producer.aar.OigusedResponseDocument.OigusedResponse;
import ee.riik.xtee.client.types.ee.riik.xtee.aar.producers.producer.aar.OigusedVastus;
import ee.riik.xtee.client.types.eu.x_road.rr.producer.RR414Document;
import ee.riik.xtee.client.types.eu.x_road.rr.producer.RR414RequestType;
import ee.riik.xtee.client.types.eu.x_road.rr.producer.RR414ResponseDocument.RR414Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.client.SoapFaultClientException;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.user.UserRole;
import tech.introduct.mailbox.utils.EstonianIdUtils;
import tech.introduct.mailbox.web.handler.ErrorInfo;
import tech.introduct.mailbox.xroad.XRoadClient;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.Instant.now;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * X-Road client adapter.
 * Implements all X-Road clients and executes X-Road query {@link RrXRoadDatabase} and {@link AarXRoadDatabase}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class XRoadClientImpl implements XRoadClient {
    private final RrXRoadDatabase rrDatabase;
    private final AarXRoadDatabase aarDatabase;

    @Override
    @Cacheable("RR414")
    @SneakyThrows
    public UserDto findUser(String identityCodes) {
        if (!EstonianIdUtils.isValid(identityCodes)) {
            return UserDto.builder()
                    .id(identityCodes)
                    .build();
        }
        var requestType = RR414RequestType.Factory.newInstance();
        requestType.setIsikukood(getDigits(identityCodes));
        var request = RR414Document.RR414.Factory.newInstance();
        request.setRequest(requestType);

        RR414Response rr414Response;
        try {
            rr414Response = rrDatabase.rr414V3(request);
        } catch (Exception e) {
            log.error("fetch user information", e);
            throw new ErrorInfo("rr414_service_unavailable").unavailable();
        }

        var response = rr414Response.getResponse();
        var faultCode = response.getFaultCode();
        if (isNoneEmpty(faultCode)) {
            var faultString = response.getFaultString();
            log.warn("when rr414V3({}) got fault {}: {}", identityCodes, faultCode, faultString);
            throw ErrorInfo.builder()
                    .error("rr414_service_error_" + faultCode)
                    .details(faultString)
                    .build().badRequest();
        }

        return UserDto.builder()
                .id(identityCodes)
                .firstName(response.getIsikuEesnimi())
                .lastName(response.getIsikuPerenimi())
                .dateOfBirth(response.getIsikuSynniaeg())
                .address(response.getIsikuElukoht())
                .build();
    }

    @Override
    @Cacheable("AAR_OIGUSED")
    @SneakyThrows
    public Set<UserRole> findRoles(String identityCodes) {
        var requestBody = OigusedParing.Factory.newInstance();
        requestBody.setIsikukood(getDigits(identityCodes));
        var request = Oigused.Factory.newInstance();
        request.setKeha(requestBody);

        OigusedResponse oigusedResponse;
        try {
            oigusedResponse = aarDatabase.oigusedV1(request);
        } catch (XRoadServiceConsumptionException ex) {
            var reason = Optional.ofNullable(ex.getSoapFaultClientException())
                    .map(SoapFaultClientException::getFaultStringOrReason)
                    .orElse(null);
            if ("Isikukood ei ole leitud".equals(trim(reason))) {
                return Set.of();
            }
            log.error("fetch roles information", ex);
            throw ErrorInfo.builder()
                    .error("aar_oigused_service_unavailable")
                    .details(reason)
                    .build().unavailable();
        } catch (Exception e) {
            log.error("fetch roles information", e);
            throw new ErrorInfo("aar_oigused_service_unavailable").unavailable();
        }

        var roles = oigusedResponse.getKeha().getOigused();
        return roles.getOigusList().stream()
                .filter(role -> StringUtils.isNotEmpty(role.getRegistrikood()))
                .filter(XRoadClientImpl::filterOigusKuni)
                .filter(XRoadClientImpl::filterOigusAlates)
                .map(OigusedVastus.Oigused.Oigus::getRegistrikood)
                .map(UserRole::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    static boolean filterOigusAlates(OigusedVastus.Oigused.Oigus role) {
        return role.getOigusAlates() == null || now().isBefore(role.getOigusAlates().toInstant());
    }

    static boolean filterOigusKuni(OigusedVastus.Oigused.Oigus role) {
        return role.getOigusKuni() == null || now().isAfter(role.getOigusKuni().toInstant());
    }
}
