package co.ravn.ecommerce.Repositories.Cart;

import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingCartDetailsRepository extends JpaRepository<ShoppingCartDetails, Integer> {
    List<ShoppingCartDetails> findByCartId(int cartId);

    List<ShoppingCartDetails> findByProductIdAndCart_Status(int productId, ShoppingCartStatusEnum status);
}
