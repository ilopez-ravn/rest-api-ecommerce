package co.ravn.ecommerce.controllers.inventory;

import co.ravn.ecommerce.dto.request.inventory.NewWarehouseRequest;
import co.ravn.ecommerce.dto.request.inventory.UpdateWarehouseRequest;
import co.ravn.ecommerce.dto.response.inventory.WarehouseResponse;
import co.ravn.ecommerce.exception.GlobalExceptionHandler;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.filters.JwtAuthFilter;
import co.ravn.ecommerce.filters.RateLimitFilter;
import co.ravn.ecommerce.services.inventory.WarehouseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/warehouses";

    @Nested
    @DisplayName("GET /api/v1/warehouses")
    class GetActiveWarehouses {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and list of warehouses")
        void list_returns200() throws Exception {
            WarehouseResponse wh = new WarehouseResponse();
            wh.setId(1);
            wh.setName("Main");
            wh.setLocation("City A");
            wh.setIs_active(true);
            when(warehouseService.getActiveWarehouses()).thenReturn(List.of(wh));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Main"))
                    .andExpect(jsonPath("$[0].location").value("City A"));

            verify(warehouseService).getActiveWarehouses();
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and empty list when no warehouses")
        void list_empty_returns200() throws Exception {
            when(warehouseService.getActiveWarehouses()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(warehouseService).getActiveWarehouses();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/warehouses")
    class CreateWarehouse {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and warehouse when valid body")
        void create_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new NewWarehouseRequest("Warehouse B", "City B"));
            WarehouseResponse response = new WarehouseResponse();
            response.setId(1);
            response.setName("Warehouse B");
            response.setLocation("City B");
            response.setIs_active(true);
            when(warehouseService.createWarehouse(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Warehouse B"));

            verify(warehouseService).createWarehouse(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new NewWarehouseRequest("", "x"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(warehouseService, never()).createWarehouse(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/warehouses/{id}")
    class UpdateWarehouse {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and updated warehouse when valid")
        void update_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new UpdateWarehouseRequest("Updated WH", "New location"));
            WarehouseResponse response = new WarehouseResponse();
            response.setId(1);
            response.setName("Updated WH");
            response.setLocation("New location");
            response.setIs_active(true);
            when(warehouseService.updateWarehouse(eq(1), any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated WH"));

            verify(warehouseService).updateWarehouse(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when warehouse not found")
        void update_notFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(new UpdateWarehouseRequest("X", "Y"));
            when(warehouseService.updateWarehouse(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Warehouse with id 999 not found"));

            mockMvc.perform(put(BASE_URL + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(warehouseService).updateWarehouse(eq(999), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is blank")
        void update_blankName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new UpdateWarehouseRequest("", "x"));

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(warehouseService, never()).updateWarehouse(eq(1), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/warehouses/{id}")
    class DeleteWarehouse {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 204 when warehouse exists")
        void delete_returns204() throws Exception {
            doNothing().when(warehouseService).deleteWarehouse(1);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(warehouseService).deleteWarehouse(1);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when warehouse not found")
        void delete_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Warehouse with id 999 not found"))
                    .when(warehouseService).deleteWarehouse(999);

            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(warehouseService).deleteWarehouse(999);
        }
    }
}
