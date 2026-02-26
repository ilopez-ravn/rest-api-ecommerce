package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.NewWarehouseRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.WarehouseResponse;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Exception.BadRequestException;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Inventory.ProductStockMapper;
import co.ravn.ecommerce.Mappers.Inventory.WarehouseMapper;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private WarehouseMapper warehouseMapper;

    @Mock
    private ProductStockMapper productStockMapper;

    @InjectMocks
    private WarehouseService warehouseService;

    @Nested
    @DisplayName("getActiveWarehouses")
    class GetActiveWarehouses {

        @Test
        @DisplayName("returns list of warehouse responses")
        void returnsList() {
            Warehouse wh = new Warehouse();
            wh.setId(1);
            wh.setName("Main");
            wh.setLocation("City A");
            wh.setIsActive(true);
            WarehouseResponse resp = new WarehouseResponse();
            resp.setId(1);
            resp.setName("Main");
            when(warehouseRepository.findByIsActiveTrue()).thenReturn(List.of(wh));
            when(warehouseMapper.toResponse(wh)).thenReturn(resp);

            List<WarehouseResponse> result = warehouseService.getActiveWarehouses();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Main");
            verify(warehouseRepository).findByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("createWarehouse")
    class CreateWarehouse {

        @Test
        @DisplayName("saves and returns warehouse when name is unique")
        void createsWhenUnique() {
            NewWarehouseRequest request = new NewWarehouseRequest("DC1", "Location 1");
            when(warehouseRepository.findAll()).thenReturn(List.of());
            Warehouse saved = new Warehouse();
            saved.setId(1);
            saved.setName("DC1");
            saved.setLocation("Location 1");
            WarehouseResponse response = new WarehouseResponse();
            response.setId(1);
            response.setName("DC1");
            when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);
            when(warehouseMapper.toResponse(saved)).thenReturn(response);

            WarehouseResponse result = warehouseService.createWarehouse(request);

            assertThat(result.getName()).isEqualTo("DC1");
            verify(warehouseRepository).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("throws BadRequestException when name already exists")
        void throwsWhenDuplicateName() {
            NewWarehouseRequest request = new NewWarehouseRequest("DC1", "Loc");
            Warehouse existing = new Warehouse();
            existing.setName("DC1");
            when(warehouseRepository.findAll()).thenReturn(List.of(existing));

            assertThatThrownBy(() -> warehouseService.createWarehouse(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("unique");
            verify(warehouseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateWarehouse")
    class UpdateWarehouse {

        @Test
        @DisplayName("updates and returns when found and name unique")
        void updatesWhenFound() {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(1);
            warehouse.setName("Old");
            warehouse.setLocation("Loc");
            UpdateWarehouseRequest request = new UpdateWarehouseRequest("New", "NewLoc");
            WarehouseResponse response = new WarehouseResponse();
            response.setId(1);
            response.setName("New");
            when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));
            when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));
            when(warehouseRepository.save(warehouse)).thenReturn(warehouse);
            when(warehouseMapper.toResponse(warehouse)).thenReturn(response);

            WarehouseResponse result = warehouseService.updateWarehouse(1, request);

            assertThat(result.getName()).isEqualTo("New");
            verify(warehouseRepository).save(warehouse);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when warehouse not found")
        void throwsWhenNotFound() {
            when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.updateWarehouse(99,
                    new UpdateWarehouseRequest("X", "Y")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
            verify(warehouseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteWarehouse")
    class DeleteWarehouse {

        @Test
        @DisplayName("deactivates warehouse when found")
        void deactivatesWhenFound() {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(1);
            warehouse.setIsActive(true);
            when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));
            when(warehouseRepository.save(warehouse)).thenReturn(warehouse);

            warehouseService.deleteWarehouse(1);

            verify(warehouseRepository).findById(1);
            assertThat(warehouse.getIsActive()).isFalse();
            verify(warehouseRepository).save(warehouse);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when warehouse not found")
        void throwsWhenNotFound() {
            when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.deleteWarehouse(99))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(warehouseRepository, never()).save(any());
        }
    }
}
