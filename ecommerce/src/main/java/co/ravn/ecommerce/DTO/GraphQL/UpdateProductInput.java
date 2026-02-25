package co.ravn.ecommerce.DTO.GraphQL;

import java.util.List;

public class UpdateProductInput {

    private Integer id;
    private String name;
    private String description;
    private Integer updatedBy;
    private Float price;
    private List<Integer> categoryList;
    private List<Integer> tagList;
    private List<ProductImageInput> imageList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public List<Integer> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Integer> categoryList) {
        this.categoryList = categoryList;
    }

    public List<Integer> getTagList() {
        return tagList;
    }

    public void setTagList(List<Integer> tagList) {
        this.tagList = tagList;
    }

    public List<ProductImageInput> getImageList() {
        return imageList;
    }

    public void setImageList(List<ProductImageInput> imageList) {
        this.imageList = imageList;
    }
}

