package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.request.inventory.AddStockRequest;
import co.ravn.ecommerce.dto.request.inventory.NewWarehouseRequest;
import co.ravn.ecommerce.dto.request.inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.dto.response.inventory.ProductStockResponse;
import co.ravn.ecommerce.dto.response.inventory.WarehouseResponse;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.entities.inventory.Warehouse;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.inventory.ProductStockMapper;
import co.ravn.ecommerce.mappers.inventory.WarehouseMapper;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.repositories.inventory.WarehouseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final WarehouseMapper warehouseMapper;
    private final ProductStockMapper productStockMapper;

    public List<WarehouseResponse> getActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue().stream()
                .map(warehouseMapper::toResponse)
                .toList();
    }

    public WarehouseResponse getWarehouseById(int id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );
        return warehouseMapper.toResponse(warehouse);
    }

    @Transactional
    public WarehouseResponse createWarehouse(NewWarehouseRequest newWarehouseRequest) {
        if (warehouseRepository.findAll().stream()
                .anyMatch(w -> w.getName().equalsIgnoreCase(newWarehouseRequest.getName()))) {
            throw new BadRequestException("Warehouse name must be unique");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(newWarehouseRequest.getName());
        warehouse.setLocation(newWarehouseRequest.getLocation());

        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse updateWarehouse(int id, UpdateWarehouseRequest updateWarehouseRequest) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );

        if (warehouseRepository.findAll().stream()
                .anyMatch(w -> w.getName().equalsIgnoreCase(updateWarehouseRequest.getName()) && w.getId() != id)) {
            throw new BadRequestException("Warehouse name must be unique");
        }

        warehouse.setName(updateWarehouseRequest.getName());
        warehouse.setLocation(updateWarehouseRequest.getLocation());

        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void deleteWarehouse(int id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );

        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
    }

    @Transactional
    public ProductStockResponse addStockToWarehouse(int id, AddStockRequest addStockRequest) {
        Product product = productRepository.findById(addStockRequest.getProductId()).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + addStockRequest.getProductId() + " not found")
        );

        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );

        ProductStock updatedStock = stockService.modifyStock(warehouse, product, addStockRequest.getType(), addStockRequest.getQuantity());
        return productStockMapper.toResponse(updatedStock);
    }
}
