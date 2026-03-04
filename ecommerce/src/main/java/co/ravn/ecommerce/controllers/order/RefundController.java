package co.ravn.ecommerce.controllers.order;

import co.ravn.ecommerce.dto.request.order.RefundRequestDto;
import co.ravn.ecommerce.dto.request.order.RefundReviewDto;
import co.ravn.ecommerce.dto.request.order.RefundStatusUpdateDto;
import co.ravn.ecommerce.dto.request.order.ReturnShippingDto;
import co.ravn.ecommerce.dto.response.order.RefundResponse;
import co.ravn.ecommerce.utils.enums.RefundStatus;
import co.ravn.ecommerce.services.order.RefundService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
@Validated
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/orders/{orderId}/refunds")
    public ResponseEntity<RefundResponse> requestRefund(
            @PathVariable @Min(1) int orderId,
            @Valid @RequestBody RefundRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refundService.requestRefund(orderId, dto));
    }

    @GetMapping("/orders/{orderId}/refunds")
    public ResponseEntity<RefundResponse> getRefundByOrder(@PathVariable @Min(1) int orderId) {
        return ResponseEntity.ok(refundService.getRefundByOrder(orderId));
    }

    @GetMapping("/refunds")
    public ResponseEntity<Page<RefundResponse>> getRefunds(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(refundService.getRefunds(status, page, size));
    }

    @PutMapping("/refunds/{refundId}/status")
    public ResponseEntity<RefundResponse> updateRefundStatus(
            @PathVariable @Min(1) int refundId,
            @Valid @RequestBody RefundStatusUpdateDto dto) {
        RefundReviewDto reviewDto = new RefundReviewDto();
        reviewDto.setManagerNotes(dto.getManagerNotes());
        if ("APPROVE".equals(dto.getAction())) {
            return ResponseEntity.ok(refundService.approveRefund(refundId, reviewDto));
        }
        return ResponseEntity.ok(refundService.denyRefund(refundId, reviewDto));
    }

    @PutMapping("/refunds/{refundId}/return-shipping")
    public ResponseEntity<RefundResponse> submitReturnShipping(
            @PathVariable @Min(1) int refundId,
            @Valid @RequestBody ReturnShippingDto dto) {
        return ResponseEntity.ok(refundService.submitReturnShipping(refundId, dto));
    }

    @PutMapping("/refunds/{refundId}/product-received")
    public ResponseEntity<RefundResponse> markProductReceived(@PathVariable @Min(1) int refundId) {
        return ResponseEntity.ok(refundService.markProductReceived(refundId));
    }
}
