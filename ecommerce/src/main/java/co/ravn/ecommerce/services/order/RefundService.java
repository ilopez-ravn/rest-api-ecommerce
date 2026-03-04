package co.ravn.ecommerce.services.order;

import co.ravn.ecommerce.dto.RefundApprovedEvent;
import co.ravn.ecommerce.dto.RefundDeniedEvent;
import co.ravn.ecommerce.dto.RefundProcessedEvent;
import co.ravn.ecommerce.dto.RefundRequestedEvent;
import co.ravn.ecommerce.dto.request.order.RefundRequestDto;
import co.ravn.ecommerce.dto.request.order.RefundReviewDto;
import co.ravn.ecommerce.dto.request.order.ReturnShippingDto;
import co.ravn.ecommerce.dto.response.order.RefundResponse;
import co.ravn.ecommerce.dto.ReturnInTransitEvent;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.inventory.ProductChangesLog;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.entities.order.DeliveryStatus;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.OrderDetails;
import co.ravn.ecommerce.entities.order.RefundRequest;
import co.ravn.ecommerce.entities.order.ReturnShipment;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.entities.order.StripePayment;
import co.ravn.ecommerce.entities.order.StripePaymentEventLog;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ConflictException;
import co.ravn.ecommerce.exception.PaymentFailureException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.order.RefundMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.repositories.inventory.ProductStockRepository;
import co.ravn.ecommerce.repositories.order.DeliveryStatusRepository;
import co.ravn.ecommerce.repositories.order.DeliveryTrackingRepository;
import co.ravn.ecommerce.repositories.order.OrderDetailsRepository;
import co.ravn.ecommerce.repositories.order.RefundRequestRepository;
import co.ravn.ecommerce.repositories.order.ReturnShipmentRepository;
import co.ravn.ecommerce.repositories.order.SaleOrderRepository;
import co.ravn.ecommerce.repositories.order.StripePaymentEventLogRepository;
import co.ravn.ecommerce.repositories.order.StripePaymentRepository;
import co.ravn.ecommerce.utils.enums.RefundStatus;
import co.ravn.ecommerce.utils.enums.RoleEnum;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class RefundService {

    private final SaleOrderRepository saleOrderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final ReturnShipmentRepository returnShipmentRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final StripePaymentRepository stripePaymentRepository;
    private final StripePaymentEventLogRepository stripePaymentEventLogRepository;
    private final ProductStockRepository productStockRepository;
    private final ProductChangesLogRepository productChangesLogRepository;
    private final UserRepository userRepository;
    private final RefundMapper refundMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public RefundResponse requestRefund(int orderId, RefundRequestDto dto) {
        SaleOrder order = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        SysUser currentUser = getCurrentUser();

        if (currentUser.getPerson() == null || currentUser.getPerson().getId() != order.getClient().getId()) {
            throw new BadRequestException("You are not authorized to request a refund for this order");
        }

        StripePayment stripePayment = stripePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        if (!"SUCCEEDED".equals(stripePayment.getPaymentStatus())) {
            throw new BadRequestException("Order is not eligible for refund");
        }

        boolean hasActiveRefund = refundRequestRepository.existsByOrderIdAndStatusNotIn(
                orderId, List.of(RefundStatus.DENIED, RefundStatus.CANCELLED));
        if (hasActiveRefund) {
            throw new ConflictException("An active refund request already exists for this order");
        }

        boolean requiresReturn = determineRequiresReturn(orderId);

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrder(order);
        refundRequest.setRequestedBy(currentUser);
        refundRequest.setStatus(RefundStatus.PENDING_REVIEW);
        refundRequest.setRequiresReturn(requiresReturn);
        refundRequest.setReason(dto.getReason());
        refundRequest.setRequestedAt(LocalDateTime.now());

        refundRequest = refundRequestRepository.save(refundRequest);
        applicationEventPublisher.publishEvent(new RefundRequestedEvent(refundRequest));

        return refundMapper.toResponse(refundRequest);
    }

    @Transactional
    public RefundResponse approveRefund(int refundId, RefundReviewDto dto) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found with id: " + refundId));

        if (refundRequest.getStatus() != RefundStatus.PENDING_REVIEW) {
            throw new BadRequestException("Refund is not in PENDING_REVIEW status");
        }

        SysUser currentUser = getCurrentUser();
        refundRequest.setReviewedBy(currentUser);
        refundRequest.setReviewedAt(LocalDateTime.now());
        refundRequest.setManagerNotes(dto.getManagerNotes());

        if (!refundRequest.isRequiresReturn()) {
            executeRefund(refundRequest);
            applicationEventPublisher.publishEvent(new RefundApprovedEvent(refundRequest));
            applicationEventPublisher.publishEvent(new RefundProcessedEvent(refundRequest));
        } else {
            refundRequest.setStatus(RefundStatus.APPROVED);
            refundRequestRepository.save(refundRequest);
            applicationEventPublisher.publishEvent(new RefundApprovedEvent(refundRequest));
        }

        return refundMapper.toResponse(refundRequest);
    }

    @Transactional
    public RefundResponse denyRefund(int refundId, RefundReviewDto dto) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found with id: " + refundId));

        if (refundRequest.getStatus() != RefundStatus.PENDING_REVIEW) {
            throw new BadRequestException("Refund is not in PENDING_REVIEW status");
        }

        if (dto.getManagerNotes() == null || dto.getManagerNotes().isBlank()) {
            throw new BadRequestException("Manager notes are required for denial");
        }

        SysUser currentUser = getCurrentUser();
        refundRequest.setStatus(RefundStatus.DENIED);
        refundRequest.setReviewedBy(currentUser);
        refundRequest.setReviewedAt(LocalDateTime.now());
        refundRequest.setManagerNotes(dto.getManagerNotes());

        refundRequest = refundRequestRepository.save(refundRequest);
        applicationEventPublisher.publishEvent(new RefundDeniedEvent(refundRequest));

        return refundMapper.toResponse(refundRequest);
    }

    @Transactional
    public RefundResponse submitReturnShipping(int refundId, ReturnShippingDto dto) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found with id: " + refundId));

        SysUser currentUser = getCurrentUser();
        SaleOrder order = refundRequest.getOrder();
        if (currentUser.getPerson() == null || currentUser.getPerson().getId() != order.getClient().getId()) {
            throw new BadRequestException("You are not authorized to submit return shipping for this refund");
        }

        if (refundRequest.getStatus() != RefundStatus.APPROVED) {
            throw new BadRequestException("Refund is not in APPROVED status");
        }

        if (!refundRequest.isRequiresReturn()) {
            throw new BadRequestException("This refund does not require a physical return");
        }

        ReturnShipment returnShipment = new ReturnShipment();
        returnShipment.setRefundRequest(refundRequest);
        returnShipment.setTrackingNumber(dto.getTrackingNumber());
        returnShipment.setCarrierName(dto.getCarrierName());
        returnShipment.setShippedAt(LocalDateTime.now());
        returnShipmentRepository.save(returnShipment);

        refundRequest.setStatus(RefundStatus.RETURN_IN_TRANSIT);
        refundRequest = refundRequestRepository.save(refundRequest);
        applicationEventPublisher.publishEvent(new ReturnInTransitEvent(refundRequest));

        return refundMapper.toResponse(refundRequest);
    }

    @Transactional
    public RefundResponse markProductReceived(int refundId) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found with id: " + refundId));

        if (refundRequest.getStatus() != RefundStatus.RETURN_IN_TRANSIT) {
            throw new BadRequestException("Refund is not in RETURN_IN_TRANSIT status");
        }

        if (!refundRequest.isRequiresReturn()) {
            throw new BadRequestException("This refund does not require a physical return");
        }

        SysUser currentUser = getCurrentUser();
        ReturnShipment returnShipment = returnShipmentRepository.findByRefundRequestId(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Return shipment not found for refund id: " + refundId));
        returnShipment.setReceivedAt(LocalDateTime.now());
        returnShipment.setReceivedBy(currentUser);
        returnShipmentRepository.save(returnShipment);

        executeRefund(refundRequest);
        applicationEventPublisher.publishEvent(new RefundProcessedEvent(refundRequest));

        return refundMapper.toResponse(refundRequest);
    }

    public RefundResponse getRefundByOrder(int orderId) {
        SaleOrder order = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        SysUser currentUser = getCurrentUser();
        RoleEnum role = currentUser.getRole().getName();
        if (RoleEnum.CLIENT == role) {
            if (currentUser.getPerson() == null || currentUser.getPerson().getId() != order.getClient().getId()) {
                throw new BadRequestException("You are not authorized to view this refund");
            }
        }

        RefundRequest refundRequest = refundRequestRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No refund request found for order id: " + orderId));

        return refundMapper.toResponse(refundRequest);
    }

    public Page<RefundResponse> getRefunds(RefundStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<RefundRequest> refundRequests;
        if (status != null) {
            refundRequests = refundRequestRepository.findByStatusOrderByRequestedAtDesc(status, pageable);
        } else {
            refundRequests = refundRequestRepository.findAllByOrderByRequestedAtDesc(pageable);
        }
        return refundRequests.map(refundMapper::toResponse);
    }

    @Transactional
    private void executeRefund(RefundRequest refundRequest) {
        SaleOrder order = refundRequest.getOrder();
        List<OrderDetails> orderDetails = orderDetailsRepository.findByOrderId(order.getId());

        StripePayment stripePayment = stripePaymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + order.getId()));

        SysUser currentUser = getCurrentUser();

        for (OrderDetails item : orderDetails) {
            Optional<ProductStock> stockOpt = productStockRepository
                    .findByWarehouseIdAndProductId(order.getWarehouse().getId(), item.getProduct().getId());
            if (stockOpt.isPresent()) {
                ProductStock stock = stockOpt.get();
                stock.setQuantity(stock.getQuantity() + item.getQuantity());
                productStockRepository.save(stock);
            }
            productChangesLogRepository.save(new ProductChangesLog(
                    item.getProduct(),
                    "Refund stock restore: refundRequestId=" + refundRequest.getId(),
                    currentUser
            ));
        }

        try {
            Refund stripeRefund = Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(stripePayment.getStripePaymentId())
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build());
            refundRequest.setStripeRefundId(stripeRefund.getId());
        } catch (StripeException e) {
            throw new PaymentFailureException("Stripe refund failed: " + e.getMessage(), e);
        }

        refundRequest.setRefundAmount(stripePayment.getAmount());
        stripePayment.setPaymentStatus("REFUNDED");
        stripePaymentRepository.save(stripePayment);

        stripePaymentEventLogRepository.save(
                new StripePaymentEventLog(stripePayment, "refund.created", "REFUNDED", "{}"));

        refundRequest.setRefundedAt(LocalDateTime.now());
        refundRequest.setStatus(RefundStatus.REFUND_PROCESSED);
        refundRequestRepository.save(refundRequest);
    }

    private boolean determineRequiresReturn(int orderId) {
        Optional<DeliveryTracking> trackingOpt = deliveryTrackingRepository.findByOrderId(orderId);
        if (trackingOpt.isEmpty()) {
            return false;
        }

        DeliveryTracking tracking = trackingOpt.get();
        DeliveryStatus currentStatus = tracking.getStatus();

        DeliveryStatus shippedStatus = deliveryStatusRepository.findByName("SHIPPED")
                .orElseThrow(() -> new ResourceNotFoundException("Delivery status 'SHIPPED' not found"));
        DeliveryStatus deliveredStatus = deliveryStatusRepository.findByName("DELIVERED")
                .orElseThrow(() -> new ResourceNotFoundException("Delivery status 'DELIVERED' not found"));

        if (currentStatus.getStepOrder() < shippedStatus.getStepOrder()) {
            return false;
        }

        if (currentStatus.getStepOrder().equals(deliveredStatus.getStepOrder())) {
            LocalDateTime deliveredAt = tracking.getUpdatedAt();
            if (deliveredAt != null && deliveredAt.isBefore(LocalDateTime.now().minusDays(30))) {
                throw new BadRequestException("Refund window expired");
            }
            return true;
        }

        throw new BadRequestException("Cannot request refund while order is in transit");
    }

    private SysUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));
    }
}
