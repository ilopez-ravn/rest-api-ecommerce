package co.ravn.ecommerce.DTO.GraphQL;

import java.util.Optional;

public class CursorPaginationInput {
    private Optional<Long> cursor;
    private int limit;

    public CursorPaginationInput(Optional<Long> cursor, int limit) {
        this.cursor = cursor;
        this.limit = limit;
    }

    public Optional<Long> getCursor() {
        return cursor;
    }

    public void setCursor(Optional<Long> cursor) {
        this.cursor = cursor;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
