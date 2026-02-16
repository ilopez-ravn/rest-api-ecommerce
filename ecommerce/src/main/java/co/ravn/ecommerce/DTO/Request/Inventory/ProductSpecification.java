package co.ravn.ecommerce.DTO.Request.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> withSearchCriteria(ProductFilterRequest filterInput) {
        return (root, query, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();

            if (filterInput.getFilter() != null && !filterInput.getFilter().isBlank()) {
                String like = "%" + filterInput.getFilter().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like)
                ));
            }

            if (filterInput.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filterInput.getMinPrice()));
            }

            if (filterInput.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filterInput.getMaxPrice()));
            }

            predicates.add(criteriaBuilder.equal(root.get("isActive"), filterInput.getIsActive()));

            if (filterInput.getCategoriesIds() != null && !filterInput.getCategoriesIds().isEmpty()) {
                query.distinct(true);
                predicates.add(root.join("categories").get("id").in(filterInput.getCategoriesIds()));
            }

            if (filterInput.getTagsId() != null && !filterInput.getTagsId().isEmpty()) {
                query.distinct(true);
                predicates.add(root.join("tags").get("id").in(filterInput.getTagsId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> withSearchCriteria(ProductFilterRequest filterInput, Integer cursor) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterInput.getFilter() != null && !filterInput.getFilter().isBlank()) {
                String like = "%" + filterInput.getFilter().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like)
                ));
            }

            if (filterInput.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filterInput.getMinPrice()));
            }

            if (filterInput.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filterInput.getMaxPrice()));
            }

            predicates.add(criteriaBuilder.equal(root.get("isActive"), filterInput.getIsActive()));

            if (filterInput.getCategoriesIds() != null && !filterInput.getCategoriesIds().isEmpty()) {
                query.distinct(true);
                predicates.add(root.join("categories").get("id").in(filterInput.getCategoriesIds()));
            }

            if (filterInput.getTagsId() != null && !filterInput.getTagsId().isEmpty()) {
                query.distinct(true);
                predicates.add(root.join("tags").get("id").in(filterInput.getTagsId()));
            }

            if (cursor != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("id"), cursor));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
