package co.ravn.ecommerce.controllers.inventory;

import co.ravn.ecommerce.dto.request.inventory.AddStockRequest;
import co.ravn.ecommerce.dto.request.inventory.NewWarehouseRequest;
import co.ravn.ecommerce.dto.request.inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.dto.response.inventory.ProductStockResponse;
import co.ravn.ecommerce.dto.response.inventory.WarehouseResponse;
import co.ravn.ecommerce.services.inventory.WarehouseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("")
    public ResponseEntity<List<WarehouseResponse>> getActiveWarehouses() {
        return ResponseEntity.ok(warehouseService.getActiveWarehouses());
    }

    @PostMapping("")
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestBody @Valid NewWarehouseRequest newWarehouseRequest) {
        return ResponseEntity.ok(warehouseService.createWarehouse(newWarehouseRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(@PathVariable @Min(1) int id, @RequestBody @Valid UpdateWarehouseRequest updateWarehouseRequest) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, updateWarehouseRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable @Min(1) int id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<ProductStockResponse> addStockToWarehouse(@PathVariable @Min(1) int id, @RequestBody @Valid AddStockRequest addStockRequest) {
        return ResponseEntity.ok(warehouseService.addStockToWarehouse(id, addStockRequest));
    }
}
