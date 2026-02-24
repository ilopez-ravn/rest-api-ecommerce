package co.ravn.ecommerce.Repositories.Cart;

import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    Optional<ShoppingCart> findByClientIdAndStatus(int clientId, ShoppingCartStatusEnum status);
    List<ShoppingCart> findByClientId(int clientId);
    Optional<ShoppingCart> findByIdAndStatus(int id, ShoppingCartStatusEnum shoppingCartStatusEnum);

    
}
