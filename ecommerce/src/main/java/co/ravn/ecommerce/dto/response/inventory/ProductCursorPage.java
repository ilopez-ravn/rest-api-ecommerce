package co.ravn.ecommerce.dto.response.inventory;

import java.util.List;

public class ProductCursorPage {
    private List<ProductResponse> content;
    private Integer nextCursor;
    private boolean hasMore;
    private long totalItems;

    public ProductCursorPage(List<ProductResponse> content, Integer nextCursor, boolean hasMore, long totalItems) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
        this.totalItems = totalItems;
    }

    public List<ProductResponse> getContent() {
        return content;
    }

    public Integer getNextCursor() {
        return nextCursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public long getTotalItems() {
        return totalItems;
    }
}
