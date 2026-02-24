package co.ravn.ecommerce.Repositories.Order;

import co.ravn.ecommerce.Entities.Order.OrderBill;
import co.ravn.ecommerce.Utils.enums.BillDocumentTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderBillRepository extends JpaRepository<OrderBill, Integer> {
    Optional<OrderBill> findByOrderId(int orderId);
    List<OrderBill> findByDocumentTypeAndIsActive(BillDocumentTypeEnum documentType, Boolean isActive);
    Optional<OrderBill> findByDocumentNumber(String documentNumber);
}
