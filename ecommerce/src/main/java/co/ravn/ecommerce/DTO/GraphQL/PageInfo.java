package co.ravn.ecommerce.DTO.GraphQL;

public class PageInfo {
    private String endCursor;
    private boolean hasNextPage;

    public PageInfo(String endCursor, boolean hasNextPage) {
        this.endCursor = endCursor;
        this.hasNextPage = hasNextPage;
    }

    public String getEndCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
}
