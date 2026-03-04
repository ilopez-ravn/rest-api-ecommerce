package co.ravn.ecommerce.repositories.inventory;

import co.ravn.ecommerce.entities.inventory.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductId(int productId);

    Optional<ProductImage> findByIdAndProductId(int id, int productId);

    void deleteByProductId(int productId);
}

