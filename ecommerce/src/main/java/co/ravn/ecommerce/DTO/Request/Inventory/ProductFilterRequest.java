package co.ravn.ecommerce.DTO.Request.Inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class ProductFilterRequest {
    private String filter;

    private List<Integer> categoriesIds;

    private List<Integer> tagsId;

    
    private int page = 1;

    private int pageSize = 20;

    private String sortBy = "name";

    private String sortOrder = "asc";

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private boolean available;

    private boolean isActive = true;

    public ProductFilterRequest() {
    }

    public ProductFilterRequest(String filter, List<Integer> categoriesIds, List<Integer> tagsId, int page, int pageSize, String sortBy, String sortOrder, BigDecimal minPrice, BigDecimal maxPrice, boolean available, boolean isActive) {
        this.filter = filter;
        this.categoriesIds = categoriesIds;
        this.tagsId = tagsId;
        this.page = page;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.available = available;
        this.isActive = isActive;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<Integer> getCategoriesIds() {
        return categoriesIds;
    }

    public void setCategoriesIds(List<Integer> categoriesIds) {
        this.categoriesIds = categoriesIds;
    }

    public List<Integer> getTagsId() {
        return tagsId;
    }

    public void setTagsId(List<Integer> tagsId) {
        this.tagsId = tagsId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
