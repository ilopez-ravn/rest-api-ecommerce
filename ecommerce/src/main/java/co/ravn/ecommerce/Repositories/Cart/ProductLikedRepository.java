package co.ravn.ecommerce.Repositories.Cart;

import co.ravn.ecommerce.Entities.Cart.ProductLiked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLikedRepository extends JpaRepository<ProductLiked, Integer> {
    List<ProductLiked> findByUserId(int userId);
    List<ProductLiked> findByProductId(int productId);
    Optional<ProductLiked> findByProductIdAndUserId(int productId, int userId);
    List<ProductLiked> findByProductIdAndHasBeenNotifiedFalseOrHasBeenNotifiedIsNull(int productId);

    @Query("SELECT pl FROM ProductLiked pl " +
           "WHERE pl.product.id = :productId " +
           "AND (pl.hasBeenNotified = false OR pl.hasBeenNotified IS NULL) " +
           "AND pl.user.isActive = true " +
           "AND pl.product.isActive = true " +
           "AND pl.product.deletedAt IS NULL")
    List<ProductLiked> findActiveUnnotifiedByProductId(@Param("productId") int productId);
}
