package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Indication {
    TOTAL_PASSED("TOTAL-PASSED"),
    TOTAL_FAILED("TOTAL-FAILED"),
    INDETERMINATE("INDETERMINATE");

    @JsonValue
    final String representation;
}
