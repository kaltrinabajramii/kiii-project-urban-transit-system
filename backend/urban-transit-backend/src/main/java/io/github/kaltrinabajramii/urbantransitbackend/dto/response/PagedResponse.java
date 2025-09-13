package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    public static <T> PagedResponse<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {

        PagedResponse<T> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);

        return response;
    }
}