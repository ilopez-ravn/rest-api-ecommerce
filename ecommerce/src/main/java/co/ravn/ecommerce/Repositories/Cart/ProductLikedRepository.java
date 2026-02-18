package co.ravn.ecommerce.Repositories.Cart;

import co.ravn.ecommerce.Entities.Cart.ProductLiked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLikedRepository extends JpaRepository<ProductLiked, Integer> {
    List<ProductLiked> findByUserId(int userId);
    List<ProductLiked> findByProductId(int productId);
    Optional<ProductLiked> findByProductIdAndUserId(int productId, int userId);
}
