package co.ravn.ecommerce.DTO.Response.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Product;

import java.util.List;

public class ProductCursorPage {
    private List<Product> content;
    private Integer nextCursor;
    private boolean hasMore;
    private long totalItems;

    public ProductCursorPage(List<Product> content, Integer nextCursor, boolean hasMore, long totalItems) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
        this.totalItems = totalItems;
    }

    public List<ProductResponse> getContent() {
        return content.stream().map(ProductResponse::new).toList();
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
