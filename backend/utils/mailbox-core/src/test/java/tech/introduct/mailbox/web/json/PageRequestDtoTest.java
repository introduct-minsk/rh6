package tech.introduct.mailbox.web.json;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageRequestDtoTest {

    @Test
    void toPageWithoutSort_expectUnsorted() {
        var request = new PageRequestDto();
        var page = request.toPage();
        assertEquals(Sort.unsorted(), page.getSort());
    }

    @Test
    void toPageWithSort_expectCorrectSort() {
        var request = new PageRequestDto();
        request.setDirection(Sort.Direction.DESC);
        request.setSort(new String[]{"test"});
        var page = request.toPage();
        assertEquals(Sort.by(request.getDirection(), request.getSort()), page.getSort());
    }
}
