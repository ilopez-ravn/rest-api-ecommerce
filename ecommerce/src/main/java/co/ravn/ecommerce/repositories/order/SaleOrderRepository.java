package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Integer>, JpaSpecificationExecutor<SaleOrder> {
    List<SaleOrder> findByClientId(int clientId);
    List<SaleOrder> findByClientIdAndIsActive(int clientId, Boolean isActive);
    Optional<SaleOrder> findByShoppingCartId(int shoppingCartId);
}
