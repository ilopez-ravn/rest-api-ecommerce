package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.DTO.GraphQL.*;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductQueryResolver {

    @Autowired
    private ProductRepository productRepository;

    @QueryMapping
    public Product getProductById(@Argument int id) {
        return productRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public ProductConnection products(@Argument("filter") ProductFilterInput productFilterInput,
                                      @Argument("pagination") CursorPaginationInput cursorPaginationInput) {
        int limit = cursorPaginationInput != null ? cursorPaginationInput.getLimit() : 10;
        Optional<Long> cursor = cursorPaginationInput != null ? cursorPaginationInput.getCursor() : Optional.empty();

        Specification<Product> specification = buildSpecification(productFilterInput, cursor.orElse(null));
        PageRequest pageRequest = PageRequest.of(0, limit + 1, Sort.by("id").ascending());

        List<Product> results = productRepository.findAll(specification, pageRequest).getContent();

        boolean hasNextPage = results.size() > limit;
        if (hasNextPage) {
            results = results.subList(0, limit);
        }

        List<ProductEdge> edges = results.stream()
                .map(product -> new ProductEdge(product, String.valueOf(product.getId())))
                .toList();

        String endCursor = results.isEmpty() ? null : String.valueOf(results.getLast().getId());
        PageInfo pageInfo = new PageInfo(endCursor, hasNextPage);

        return new ProductConnection(edges, pageInfo);


    }


    private Specification<Product> buildSpecification(ProductFilterInput filterInput, Long cursor) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cursor != null) {
                predicates.add(builder.greaterThan(root.get("id"), cursor));
            }

            if (filterInput != null) {
                if (filterInput.getFilter() != null && !filterInput.getFilter().isBlank()) {
                    String like = "%" + filterInput.getFilter().toLowerCase() + "%";
                    predicates.add(builder.or(
                            builder.like(builder.lower(root.get("name")), like),
                            builder.like(builder.lower(root.get("description")), like)
                    ));
                }

                if (filterInput.getMinPrice() != null) {
                    predicates.add(builder.greaterThanOrEqualTo(root.get("price"), filterInput.getMinPrice()));
                }

                if (filterInput.getMaxPrice() != null) {
                    predicates.add(builder.lessThanOrEqualTo(root.get("price"), filterInput.getMaxPrice()));
                }

                if (filterInput.getIsActive() != null) {
                    predicates.add(builder.equal(root.get("isActive"), filterInput.getIsActive()));
                }

                if (filterInput.getCategoryIds() != null && !filterInput.getCategoryIds().isEmpty()) {
                    query.distinct(true);
                    predicates.add(root.join("categories").get("id").in(filterInput.getCategoryIds()));
                }

                if (filterInput.getTagIds() != null && !filterInput.getTagIds().isEmpty()) {
                    query.distinct(true);
                    predicates.add(root.join("tags").get("id").in(filterInput.getTagIds()));
                }
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
