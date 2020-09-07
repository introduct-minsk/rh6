package tech.introduct.mailbox.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import feign.FeignException;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Profile("!test")
@FeignClient(url = "${feign.client.search-service.url}", name = "search-service")
public interface SearchClient {

    default Page<UUID> findSourceIds(String role, String query, PageRequest pageable) {
        try {
            return apiFindSourceIds(role, query, pageable.getPageNumber(), pageable.getPageSize());
        } catch (FeignException.BadRequest e) {
            return Page.empty(pageable);
        } catch (Exception e) {
            LogHolder.log.error("find source ids", e);
            throw new ErrorInfo("search_service_unavailable").unavailable();
        }
    }

    default void createSource(SourceRequest dto) {
        try {
            apiCreateSource(dto);
        } catch (Exception e) {
            LogHolder.log.error("create source", e);
            throw new ErrorInfo("search_service_unavailable").unavailable();
        }
    }

    @PostMapping(path = "/private/api/search/sources",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void apiCreateSource(@RequestBody SourceRequest dto);

    @GetMapping(path = "/private/api/search/sources/id", consumes = MediaType.APPLICATION_JSON_VALUE)
    UUIDPage apiFindSourceIds(@RequestParam("role") String role, @RequestParam("query") String query,
                              @RequestParam("page") int page, @RequestParam("size") int size);

    @Value
    class SourceRequest {
        UUID id;
        ZonedDateTime date;
        String sender;
        String receiver;
        String value;
    }

    class UUIDPage extends PageImpl<UUID> {

        @JsonCreator
        public UUIDPage(@JsonProperty("content") List<UUID> content,
                        @JsonProperty("number") int number,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") long totalElements) {
            super(content, PageRequest.of(number, size), totalElements);
        }
    }

    @Slf4j
    @UtilityClass
    final class LogHolder {
    }
}
