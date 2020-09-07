package tech.introduct.mailbox.web.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import tech.introduct.mailbox.dto.MailboxSourceRequest;
import tech.introduct.mailbox.service.SearchService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/private/api/search", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Secured("ROLE_SEARCH_SOURCE")
public class PrivateSearchController {
    private final SearchService searchService;

    @PostMapping(path = "/sources", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createSource(@RequestBody @Valid MailboxSourceRequest dto) {
        searchService.save(dto);
    }

    @GetMapping("/sources/id")
    public Page<String> findSourceIds(@Valid FindSourceRequest request) {
        var page = PageRequest.of(request.page, request.size);
        return searchService.findIds(request.query, request.role, page);
    }

    @Data
    static class FindSourceRequest {
        @NotNull
        private String role;
        @NotNull
        private String query;
        @Min(0)
        private int page = 0;
        @Min(5)
        @Max(50)
        private int size = 10;
    }
}
