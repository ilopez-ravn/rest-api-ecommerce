package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.RefundRequest;
import co.ravn.ecommerce.utils.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {
    Optional<RefundRequest> findByOrderId(int orderId);
    boolean existsByOrderIdAndStatusNotIn(int orderId, List<RefundStatus> statuses);
    Page<RefundRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
    Page<RefundRequest> findByStatusOrderByRequestedAtDesc(RefundStatus status, Pageable pageable);
}
