package tech.introduct.mailbox.web.json;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.springframework.data.domain.PageRequest.of;

@Data
public class PageRequestDto {
    private int page = 0;
    private int size = 10;
    private Sort.Direction direction = Sort.DEFAULT_DIRECTION;
    private String[] sort = new String[]{};

    public PageRequest toPage() {
        if (sort.length == 0) {
            return of(page, size);
        }
        return of(page, size, direction, sort);
    }
}
