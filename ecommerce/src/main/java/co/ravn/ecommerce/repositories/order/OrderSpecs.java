package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.DeliveryStatus;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.SaleOrder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderSpecs {

    private OrderSpecs() {}

    public static Specification<SaleOrder> withFilters(
            Integer clientId,
            String status,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), dateTo));
            }
            if (status != null && !status.isBlank()) {
                Join<SaleOrder, DeliveryTracking> dtJoin = root.join("deliveryTrackings", JoinType.LEFT);
                Join<DeliveryTracking, DeliveryStatus> dsJoin = dtJoin.join("status", JoinType.LEFT);
                predicates.add(cb.equal(dsJoin.get("name"), status));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
