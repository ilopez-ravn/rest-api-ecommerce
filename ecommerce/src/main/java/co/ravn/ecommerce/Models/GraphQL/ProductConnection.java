package co.ravn.ecommerce.Models.GraphQL;

import java.util.List;

public class ProductConnection {
    private List<ProductEdge> edges;
    private PageInfo pageInfo;

    public ProductConnection(List<ProductEdge> edges, PageInfo pageInfo) {
        this.edges = edges;
        this.pageInfo = pageInfo;
    }

    public List<ProductEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<ProductEdge> edges) {
        this.edges = edges;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
