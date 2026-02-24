package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.AddStockRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.NewWarehouseRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.UpdateWarehouseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.ravn.ecommerce.Services.Inventory.WarehouseService;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("")
    public ResponseEntity<?> getActiveWarehouses() {
        return warehouseService.getActiveWarehouses();
    }

    @PostMapping("")
    public ResponseEntity<?> createWarehouse(@RequestBody @Valid NewWarehouseRequest newWarehouseRequest) {
        return warehouseService.createWarehouse(newWarehouseRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWarehouse(@PathVariable @Min(1) int id, @RequestBody @Valid UpdateWarehouseRequest updateWarehouseRequest) {
        return warehouseService.updateWarehouse(id, updateWarehouseRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWarehouse(@PathVariable @Min(1) int id) {
        return warehouseService.deleteWarehouse(id);
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<?> addStockToWarehouse(@PathVariable @Min(1) int id, @RequestBody @Valid AddStockRequest addStockRequest) {
        return warehouseService.addStockToWarehouse(id, addStockRequest);
    }

}
