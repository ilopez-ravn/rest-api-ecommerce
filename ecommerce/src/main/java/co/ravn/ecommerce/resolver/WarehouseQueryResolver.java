package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.response.inventory.WarehouseResponse;
import co.ravn.ecommerce.services.inventory.WarehouseService;
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
