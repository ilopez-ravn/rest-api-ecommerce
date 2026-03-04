package co.ravn.ecommerce.dto.response.order;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PaginatedOrderResponse {

    private PageInfo page_info;
    private List<OrderResponse> items;

    public PaginatedOrderResponse(Page<?> page, List<OrderResponse> items) {
        this.page_info = new PageInfo(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        this.items = items;
    }

    @Getter
    public static class PageInfo {
        private final int current_page;
        private final int page_size;
        private final int total_pages;
        private final long total_items;

        public PageInfo(int currentPage, int pageSize, int totalPages, long totalItems) {
            this.current_page = currentPage;
            this.page_size = pageSize;
            this.total_pages = totalPages;
            this.total_items = totalItems;
        }
    }
}
