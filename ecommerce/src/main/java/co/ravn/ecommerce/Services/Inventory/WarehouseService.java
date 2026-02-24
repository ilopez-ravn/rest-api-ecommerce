package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.AddStockRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.NewWarehouseRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.WarehouseResponse;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Inventory.ProductStockMapper;
import co.ravn.ecommerce.Mappers.Inventory.WarehouseMapper;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.WarehouseRepository;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final WarehouseMapper warehouseMapper;
    private final ProductStockMapper productStockMapper;

    public ResponseEntity<?> getActiveWarehouses() {
        List<Warehouse> activeWarehouses = warehouseRepository.findByIsActiveTrue();
        List<WarehouseResponse> body = activeWarehouses.stream().map(warehouseMapper::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @Transactional
    public ResponseEntity<?> createWarehouse(NewWarehouseRequest newWarehouseRequest) {
        // check that warehouse name is unique
        if (warehouseRepository.findAll().stream().anyMatch(w -> w.getName().equalsIgnoreCase(newWarehouseRequest.getName()))) {
            return ResponseEntity.badRequest().body("Warehouse name must be unique");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(newWarehouseRequest.getName());
        warehouse.setLocation(newWarehouseRequest.getLocation());

        Warehouse saved = warehouseRepository.save(warehouse);
        return ResponseEntity.ok(warehouseMapper.toResponse(saved));
    }

    @Transactional
    public ResponseEntity<?> updateWarehouse(int id, UpdateWarehouseRequest updateWarehouseRequest) {
        //search by id or return not found error
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );

        // check that warehouse name is unique ignoring the current warehouse
        if (warehouseRepository.findAll().stream()
                .anyMatch(w -> w.getName().equalsIgnoreCase(updateWarehouseRequest.getName()) && w.getId() != id)) {
            return ResponseEntity.badRequest().body("Warehouse name must be unique");
        }

        warehouse.setName(updateWarehouseRequest.getName());
        warehouse.setLocation(updateWarehouseRequest.getLocation());

        Warehouse saved = warehouseRepository.save(warehouse);
        return ResponseEntity.ok(warehouseMapper.toResponse(saved));
    }

    @Transactional
    public ResponseEntity<?> deleteWarehouse(int id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );

        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> addStockToWarehouse(int id, AddStockRequest addStockRequest) {
        // Check product id and warehouse id exist
        Product product = productRepository.findById(addStockRequest.getProductId()).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + addStockRequest.getProductId() + " not found")
        );

        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Warehouse with id " + id + " not found")
        );

        ProductStock updatedStock = stockService.modifyStock(warehouse, product, addStockRequest.getType(), addStockRequest.getQuantity());
        return ResponseEntity.ok(productStockMapper.toResponse(updatedStock));
    }
}   
