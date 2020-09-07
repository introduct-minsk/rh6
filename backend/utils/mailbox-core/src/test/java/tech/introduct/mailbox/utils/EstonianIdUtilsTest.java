package tech.introduct.mailbox.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EstonianIdUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "50307120003",
            "50307120014",
            "50307120025",
            "39401120014",
            "39401120025",
            "39402120074",
            "60407010005",
            "60407010016",
            "60407010027",
            "49701170004",
            "49701170015",
            "49701170026",
            "49701170037",
            "EE49701170048",
            "36002120187",
            "36002120198",
            "36002120208",
            "36002120219",
            "36002120226",
            "36002120230",
            "54001010461",
            "54001010472",
            "48001010005",
            "48001010010",
            "48001010021",
            "48001010032",
            "48001010043",
            "48001010054",
            "48001010065",
            "48001010076",
            "48001010087",
            "11111112350",
            "48001010098"
    })
    void validateCorrectId(String id) {
        assertTrue(EstonianIdUtils.isValid(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test",
            "50307120024",
            "50307120026",
            "39401120214",
            "p9401120025",
            "49701 170004",
            "49701170026 я ",
            "",
    })
    void validateWrongId(String id) {
        assertFalse(EstonianIdUtils.isValid(id));
    }

    @Test
    void validateWithNull() {
        assertFalse(EstonianIdUtils.isValid(null));
    }
}
