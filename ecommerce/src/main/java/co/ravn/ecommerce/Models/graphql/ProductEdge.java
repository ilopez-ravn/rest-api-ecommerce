package co.ravn.ecommerce.models.graphql;


import co.ravn.ecommerce.entities.inventory.Product;

public class ProductEdge {
    private Product node;
    private String cursor;

    public ProductEdge(Product node, String cursor) {
        this.node = node;
        this.cursor = cursor;
    }

    public Product getNode() {
        return node;
    }

    public void setNode(Product node) {
        this.node = node;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
