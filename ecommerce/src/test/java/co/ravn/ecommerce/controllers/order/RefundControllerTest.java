package co.ravn.ecommerce.controllers.order;

import co.ravn.ecommerce.dto.request.order.RefundRequestDto;
import co.ravn.ecommerce.dto.request.order.RefundReviewDto;
import co.ravn.ecommerce.dto.request.order.RefundStatusUpdateDto;
import co.ravn.ecommerce.dto.request.order.ReturnShippingDto;
import co.ravn.ecommerce.dto.response.order.RefundResponse;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ConflictException;
import co.ravn.ecommerce.exception.GlobalExceptionHandler;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.filters.JwtAuthFilter;
import co.ravn.ecommerce.filters.RateLimitFilter;
import co.ravn.ecommerce.services.order.RefundService;
import co.ravn.ecommerce.utils.enums.RefundStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RefundController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RefundService refundService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private RefundResponse buildRefundResponse(int id, int orderId, RefundStatus status) {
        RefundResponse response = new RefundResponse();
        response.setId(id);
        response.setOrder_id(orderId);
        response.setStatus(status.getValue());
        response.setReason("Test reason");
        response.setRefund_amount(new BigDecimal("100.00"));
        return response;
    }

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/refunds")
    class RequestRefund {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 201 Created when valid request as CLIENT")
        void returns201OnValidRequest() throws Exception {
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Defective product");
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.PENDING_REVIEW);

            when(refundService.requestRefund(eq(1), any(RefundRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/orders/1/refunds")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));

            verify(refundService).requestRefund(eq(1), any(RefundRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when order is not eligible")
        void returns400WhenIneligible() throws Exception {
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Test");

            when(refundService.requestRefund(eq(1), any(RefundRequestDto.class)))
                    .thenThrow(new BadRequestException("Order is not eligible for refund"));

            mockMvc.perform(post("/api/v1/orders/1/refunds")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 409 when duplicate active refund")
        void returns409WhenDuplicate() throws Exception {
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Test");

            when(refundService.requestRefund(eq(1), any(RefundRequestDto.class)))
                    .thenThrow(new ConflictException("An active refund request already exists"));

            mockMvc.perform(post("/api/v1/orders/1/refunds")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}/refunds")
    class GetRefundByOrder {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 with refund response for own order")
        void returns200ForOwnOrder() throws Exception {
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.PENDING_REVIEW);
            when(refundService.getRefundByOrder(1)).thenReturn(response);

            mockMvc.perform(get("/api/v1/orders/1/refunds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.order_id").value(1));

            verify(refundService).getRefundByOrder(1);
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when refund not found")
        void returns404WhenNotFound() throws Exception {
            when(refundService.getRefundByOrder(99))
                    .thenThrow(new ResourceNotFoundException("No refund request found for order id: 99"));

            mockMvc.perform(get("/api/v1/orders/99/refunds"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/refunds")
    class GetRefunds {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 with page of refunds for MANAGER")
        void returns200WithPageForManager() throws Exception {
            RefundResponse r1 = buildRefundResponse(1, 1, RefundStatus.PENDING_REVIEW);
            RefundResponse r2 = buildRefundResponse(2, 2, RefundStatus.APPROVED);
            PageImpl<RefundResponse> page = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 10), 2);

            when(refundService.getRefunds(null, 0, 10)).thenReturn(page);

            mockMvc.perform(get("/api/v1/refunds")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 with filtered results when status param is set")
        void returns200WithStatusFilter() throws Exception {
            RefundResponse r1 = buildRefundResponse(1, 1, RefundStatus.PENDING_REVIEW);
            PageImpl<RefundResponse> page = new PageImpl<>(List.of(r1), PageRequest.of(0, 10), 1);

            when(refundService.getRefunds(eq(RefundStatus.PENDING_REVIEW), eq(0), eq(10))).thenReturn(page);

            mockMvc.perform(get("/api/v1/refunds")
                            .param("status", "PENDING_REVIEW")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/refunds/{refundId}/status")
    class UpdateRefundStatus {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 when MANAGER approves refund")
        void returns200WhenApproved() throws Exception {
            RefundStatusUpdateDto dto = new RefundStatusUpdateDto();
            dto.setAction("APPROVE");
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.REFUND_PROCESSED);

            when(refundService.approveRefund(eq(1), any(RefundReviewDto.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUND_PROCESSED"));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 when MANAGER denies refund with notes")
        void returns200WhenDenied() throws Exception {
            RefundStatusUpdateDto dto = new RefundStatusUpdateDto();
            dto.setAction("DENY");
            dto.setManagerNotes("Not eligible per policy");
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.DENIED);

            when(refundService.denyRefund(eq(1), any(RefundReviewDto.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DENIED"));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when refund is in wrong status")
        void returns400WhenWrongStatus() throws Exception {
            RefundStatusUpdateDto dto = new RefundStatusUpdateDto();
            dto.setAction("APPROVE");

            when(refundService.approveRefund(eq(1), any(RefundReviewDto.class)))
                    .thenThrow(new BadRequestException("Refund is not in PENDING_REVIEW status"));

            mockMvc.perform(put("/api/v1/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when blank manager notes on deny")
        void returns400WhenBlankNotesOnDeny() throws Exception {
            RefundStatusUpdateDto dto = new RefundStatusUpdateDto();
            dto.setAction("DENY");

            when(refundService.denyRefund(eq(1), any(RefundReviewDto.class)))
                    .thenThrow(new BadRequestException("Manager notes are required for denial"));

            mockMvc.perform(put("/api/v1/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when action is invalid")
        void returns400WhenInvalidAction() throws Exception {
            mockMvc.perform(put("/api/v1/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"action\":\"INVALID\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/refunds/{refundId}/return-shipping")
    class SubmitReturnShipping {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 when CLIENT submits return shipping")
        void returns200WhenSubmitted() throws Exception {
            ReturnShippingDto dto = new ReturnShippingDto();
            dto.setTrackingNumber("TRK123");
            dto.setCarrierName("FedEx");
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.RETURN_IN_TRANSIT);

            when(refundService.submitReturnShipping(eq(1), any(ReturnShippingDto.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/refunds/1/return-shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETURN_IN_TRANSIT"));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when refund does not require physical return")
        void returns400WhenNoReturnRequired() throws Exception {
            ReturnShippingDto dto = new ReturnShippingDto();
            dto.setTrackingNumber("TRK123");
            dto.setCarrierName("FedEx");

            when(refundService.submitReturnShipping(eq(1), any(ReturnShippingDto.class)))
                    .thenThrow(new BadRequestException("This refund does not require a physical return"));

            mockMvc.perform(put("/api/v1/refunds/1/return-shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/refunds/{refundId}/product-received")
    class MarkProductReceived {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 when MANAGER marks product received")
        void returns200ForManager() throws Exception {
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.REFUND_PROCESSED);
            when(refundService.markProductReceived(1)).thenReturn(response);

            mockMvc.perform(put("/api/v1/refunds/1/product-received"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUND_PROCESSED"));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE")
        @DisplayName("returns 200 when WAREHOUSE marks product received")
        void returns200ForWarehouse() throws Exception {
            RefundResponse response = buildRefundResponse(1, 1, RefundStatus.REFUND_PROCESSED);
            when(refundService.markProductReceived(1)).thenReturn(response);

            mockMvc.perform(put("/api/v1/refunds/1/product-received"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when refund is in wrong status")
        void returns400WhenWrongStatus() throws Exception {
            when(refundService.markProductReceived(1))
                    .thenThrow(new BadRequestException("Refund is not in RETURN_IN_TRANSIT status"));

            mockMvc.perform(put("/api/v1/refunds/1/product-received"))
                    .andExpect(status().isBadRequest());
        }
    }
}
