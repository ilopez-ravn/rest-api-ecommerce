package co.ravn.ecommerce.dto.request.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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

    private Boolean available;

    private Boolean isActive = true;

}
