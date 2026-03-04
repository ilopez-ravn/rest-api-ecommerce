package co.ravn.ecommerce.repositories.cart;

import co.ravn.ecommerce.entities.cart.ShoppingCart;
import co.ravn.ecommerce.utils.enums.ShoppingCartStatusEnum;

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
