package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductChangesLog;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.StockOperationType;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductStockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private ProductChangesLogRepository productChangesLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockService stockService;

    private Warehouse buildWarehouse() {
        Warehouse w = new Warehouse();
        w.setId(1);
        w.setName("Main Warehouse");
        w.setIsActive(true);
        return w;
    }

    private Product buildProduct() {
        Product p = new Product();
        p.setId(2);
        p.setName("Widget");
        return p;
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("modifyStock - ADD operation")
    class ModifyStockAdd {

        @Test
        @DisplayName("creates new stock record with given quantity when none exists")
        void createsNewStockRecord() {
            authenticate("manager1");
            SysUser user = new SysUser();
            user.setId(10);
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.empty());
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(productChangesLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(productStockRepository.save(any(ProductStock.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductStock result = stockService.modifyStock(warehouse, product, StockOperationType.ADD, 5);

            assertThat(result.getQuantity()).isEqualTo(5);
            verify(productStockRepository).save(any(ProductStock.class));
        }

        @Test
        @DisplayName("adds quantity to an existing stock record")
        void addsQuantityToExistingRecord() {
            authenticate("manager1");
            SysUser user = new SysUser();
            user.setId(10);
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();
            ProductStock existing = new ProductStock();
            existing.setQuantity(10);

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.of(existing));
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(productChangesLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(productStockRepository.save(any(ProductStock.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductStock result = stockService.modifyStock(warehouse, product, StockOperationType.ADD, 3);

            assertThat(result.getQuantity()).isEqualTo(13);
        }
    }

    @Nested
    @DisplayName("modifyStock - SUBTRACT operation")
    class ModifyStockSubtract {

        @Test
        @DisplayName("reduces quantity when there is enough stock")
        void reducesQuantityWithSufficientStock() {
            authenticate("manager1");
            SysUser user = new SysUser();
            user.setId(10);
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();
            ProductStock existing = new ProductStock();
            existing.setQuantity(10);

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.of(existing));
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(productChangesLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(productStockRepository.save(any(ProductStock.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductStock result = stockService.modifyStock(warehouse, product, StockOperationType.SUBTRACT, 4);

            assertThat(result.getQuantity()).isEqualTo(6);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when stock is insufficient")
        void throwsWhenInsufficientStock() {
            authenticate("manager1");
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();
            ProductStock existing = new ProductStock();
            existing.setQuantity(2);

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> stockService.modifyStock(warehouse, product, StockOperationType.SUBTRACT, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not enough stock");
            verify(productStockRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when subtracting exact stock amount that exceeds zero-quantity record")
        void throwsWhenSubtractingFromEmptyStock() {
            authenticate("manager1");
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();
            ProductStock existing = new ProductStock();
            existing.setQuantity(0);

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> stockService.modifyStock(warehouse, product, StockOperationType.SUBTRACT, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not enough stock");
        }
    }

    @Nested
    @DisplayName("modifyStock - auth and logging")
    class ModifyStockAuthAndLogging {

        @Test
        @DisplayName("saves a change log entry after a successful modification")
        void savesChangeLogOnSuccess() {
            authenticate("manager1");
            SysUser user = new SysUser();
            user.setId(10);
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();
            ProductStock existing = new ProductStock();
            existing.setQuantity(5);

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.of(existing));
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(productChangesLogRepository.save(any(ProductChangesLog.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productStockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            stockService.modifyStock(warehouse, product, StockOperationType.ADD, 2);

            verify(productChangesLogRepository).save(any(ProductChangesLog.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when authenticated user is not found in the database")
        void throwsWhenUserNotFound() {
            authenticate("ghost");
            Warehouse warehouse = buildWarehouse();
            Product product = buildProduct();
            ProductStock existing = new ProductStock();
            existing.setQuantity(10);

            when(productStockRepository.findByWarehouseIdAndProductId(1, 2)).thenReturn(Optional.of(existing));
            when(userRepository.findByUsernameAndIsActiveTrue("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.modifyStock(warehouse, product, StockOperationType.ADD, 5))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
            verify(productStockRepository, never()).save(any());
        }
    }
}
