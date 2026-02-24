package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.AddStockRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.NewWarehouseRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductStockResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.WarehouseResponse;
import co.ravn.ecommerce.Services.Inventory.WarehouseService;
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
