package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.request.inventory.AddStockRequest;
import co.ravn.ecommerce.dto.request.inventory.NewWarehouseRequest;
import co.ravn.ecommerce.dto.request.inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.dto.response.inventory.ProductStockResponse;
import co.ravn.ecommerce.dto.response.inventory.WarehouseResponse;
import co.ravn.ecommerce.entities.inventory.StockOperationType;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.services.inventory.WarehouseService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@AllArgsConstructor
public class WarehouseMutationResolver {

    private final WarehouseService warehouseService;

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public WarehouseResponse createWarehouse(@Argument String name, @Argument String location) {
        return warehouseService.createWarehouse(new NewWarehouseRequest(name, location));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public WarehouseResponse updateWarehouse(@Argument int id, @Argument String name, @Argument String location) {
        return warehouseService.updateWarehouse(id, new UpdateWarehouseRequest(name, location != null ? location : ""));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Boolean deleteWarehouse(@Argument int id) {
        warehouseService.deleteWarehouse(id);
        return true;
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAREHOUSE')")
    @Transactional
    public ProductStockResponse updateProductStock(
            @Argument int productId,
            @Argument int warehouseId,
            @Argument int quantity,
            @Argument String type) {
        StockOperationType operationType = parseStockOperationType(type);
        AddStockRequest request = new AddStockRequest(productId, operationType, quantity);
        return warehouseService.addStockToWarehouse(warehouseId, request);
    }

    private static StockOperationType parseStockOperationType(String type) {
        if (type == null || type.isBlank()) {
            throw new BadRequestException("Stock operation type is required (ADD or SUBTRACT)");
        }
        return switch (type.toUpperCase()) {
            case "ADD" -> StockOperationType.ADD;
            case "SUBTRACT" -> StockOperationType.SUBTRACT;
            default -> throw new BadRequestException("Invalid stock operation type: " + type + ". Use ADD or SUBTRACT.");
        };
    }
}
