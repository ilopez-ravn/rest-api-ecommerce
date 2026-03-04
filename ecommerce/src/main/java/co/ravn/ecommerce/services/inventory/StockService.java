package co.ravn.ecommerce.services.inventory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.entities.inventory.ProductChangesLog;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.entities.inventory.StockOperationType;
import co.ravn.ecommerce.entities.inventory.Warehouse;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.repositories.inventory.ProductStockRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StockService {

    private ProductStockRepository productStockRepository;
    private final ProductChangesLogRepository productChangesLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductStock modifyStock(Warehouse warehouse, Product product, StockOperationType type, int quantity) {
        // check if stock record exists for this warehouse and product
        ProductStock stock = productStockRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseGet(() -> {
                    // if not, create a new stock record with quantity 0
                    ProductStock newStock = new ProductStock();
                    newStock.setWarehouse(warehouse);
                    newStock.setProduct(product);
                    newStock.setQuantity(0);
                    return newStock;
                });

        // update the stock quantity based on operation type
        int currentQuantity = stock.getQuantity();
        int newQuantity = stock.getQuantity();
        if (type == StockOperationType.ADD) {
            newQuantity += quantity;
        } else if (type == StockOperationType.SUBTRACT) {
            // Check if there is enough stock to subtract
            if (currentQuantity < quantity) {
                throw new IllegalArgumentException("Not enough stock to subtract. Current stock: " + currentQuantity);
            }
            newQuantity -= quantity;
        }

        // Register the changes in ProductChangesLog
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));

        ProductChangesLog logEntry = new ProductChangesLog(
                product,
                "Stock change from " + currentQuantity + " to " + newQuantity + " in warehouse " + warehouse.getName(),
                loggedInUser
        );

        productChangesLogRepository.save(logEntry);
        stock.setQuantity(newQuantity);
        return productStockRepository.save(stock);
    }


}
