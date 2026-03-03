package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.Response.Inventory.WarehouseResponse;
import co.ravn.ecommerce.Services.Inventory.WarehouseService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class WarehouseQueryResolver {

    private final WarehouseService warehouseService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAREHOUSE')")
    public WarehouseResponse getWarehouseById(@Argument int id) {
        return warehouseService.getWarehouseById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAREHOUSE')")
    public List<WarehouseResponse> warehouses() {
        return warehouseService.getActiveWarehouses();
    }
}
