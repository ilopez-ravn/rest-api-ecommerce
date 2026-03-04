package co.ravn.ecommerce.dto.graphql;

import co.ravn.ecommerce.dto.response.order.PaginatedOrderResponse;
import co.ravn.ecommerce.dto.response.order.OrderResponse;

import java.util.List;

public class OrderPage {

    private List<OrderResponse> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;

    public OrderPage(PaginatedOrderResponse source) {
        this.content = source.getItems();
        this.totalPages = source.getPage_info().getTotal_pages();
        this.totalElements = source.getPage_info().getTotal_items();
        this.number = source.getPage_info().getCurrent_page();
        this.size = source.getPage_info().getPage_size();
    }

    public List<OrderResponse> getContent() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }
}

