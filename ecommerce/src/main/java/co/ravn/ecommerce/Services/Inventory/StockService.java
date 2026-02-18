package co.ravn.ecommerce.Services.Inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductChangesLog;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.StockOperationType;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductStockRepository;

@Service
public class StockService {
    
private ProductStockRepository productStockRepository;
    private final ProductChangesLogRepository productChangesLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public StockService(ProductStockRepository productStockRepository, ProductChangesLogRepository productChangesLogRepository, UserRepository userRepository) {
        this.productStockRepository = productStockRepository;
        this.productChangesLogRepository = productChangesLogRepository;
        this.userRepository = userRepository;
    }

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
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));

        ProductChangesLog logEntry = new ProductChangesLog(
            product,
            "Stock change from "    + currentQuantity + " to " + newQuantity + " in warehouse " + warehouse.getName(),
            loggedInUser
        );

        productChangesLogRepository.save(logEntry);
        stock.setQuantity(newQuantity);
        return productStockRepository.save(stock);
    }


}
