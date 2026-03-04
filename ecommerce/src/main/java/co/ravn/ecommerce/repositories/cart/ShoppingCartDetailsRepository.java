package co.ravn.ecommerce.repositories.cart;

import co.ravn.ecommerce.entities.cart.ShoppingCartDetails;
import co.ravn.ecommerce.utils.enums.ShoppingCartStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingCartDetailsRepository extends JpaRepository<ShoppingCartDetails, Integer> {
    List<ShoppingCartDetails> findByCartId(int cartId);

    List<ShoppingCartDetails> findByProductIdAndCart_Status(int productId, ShoppingCartStatusEnum status);
}
