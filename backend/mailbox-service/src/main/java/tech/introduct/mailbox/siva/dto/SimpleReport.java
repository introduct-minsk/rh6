package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class SimpleReport {
    ValidationConclusion validationConclusion;
}
