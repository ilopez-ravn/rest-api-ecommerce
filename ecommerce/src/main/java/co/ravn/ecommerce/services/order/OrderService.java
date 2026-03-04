package co.ravn.ecommerce.services.order;

import co.ravn.ecommerce.dto.request.order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.dto.response.order.DeliveryStatusResponse;
import co.ravn.ecommerce.dto.response.order.OrderResponse;
import co.ravn.ecommerce.dto.response.order.PaginatedOrderResponse;
import co.ravn.ecommerce.dto.response.order.ShippingDetailsResponse;
import co.ravn.ecommerce.dto.DeliveryStatusChangedEvent;
import co.ravn.ecommerce.entities.clients.ClientAddress;
import co.ravn.ecommerce.entities.order.DeliveryStatus;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.OrderBill;
import co.ravn.ecommerce.entities.order.OrderDetails;
import co.ravn.ecommerce.entities.order.OrderTrackingLog;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.entities.order.StripePayment;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.order.OrderMapper;
import co.ravn.ecommerce.mappers.order.ShippingDetailsMapper;
import co.ravn.ecommerce.mappers.order.DeliveryStatusMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.order.DeliveryStatusRepository;
import co.ravn.ecommerce.repositories.order.DeliveryTrackingRepository;
import co.ravn.ecommerce.repositories.order.OrderBillRepository;
import co.ravn.ecommerce.repositories.order.OrderDetailsRepository;
import co.ravn.ecommerce.repositories.order.OrderSpecs;
import co.ravn.ecommerce.repositories.order.OrderTrackingLogRepository;
import co.ravn.ecommerce.repositories.order.SaleOrderRepository;
import co.ravn.ecommerce.repositories.order.StripePaymentRepository;
import co.ravn.ecommerce.utils.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class OrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderBillRepository orderBillRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final OrderTrackingLogRepository orderTrackingLogRepository;
    private final StripePaymentRepository stripePaymentRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ShippingDetailsMapper shippingDetailsMapper;
    private final DeliveryStatusMapper deliveryStatusMapper;
    private final ApplicationEventPublisher applicationEventPublisher;


    public PaginatedOrderResponse getOrders(
            Integer clientId,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder,
            String dateFrom,
            String dateTo
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));

        // Non-managers can only see their own orders; ignore client_id from request
        if (!RoleEnum.MANAGER.toString().equals(currentUser.getRole().getName().toString())) {
            if (currentUser.getPerson() == null) {
                throw new AccessDeniedException("Access denied");
            }
            clientId = currentUser.getPerson().getId();
        }

        String sortField = resolveSortField(sortBy);
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, sort);

        LocalDateTime from = parseDateParam(dateFrom, false);
        LocalDateTime to = parseDateParam(dateTo, true);

        Page<SaleOrder> ordersPage = saleOrderRepository.findAll(
                OrderSpecs.withFilters(clientId, status, from, to), pageable
        );

        List<OrderResponse> items = ordersPage.getContent().stream()
                .map(this::buildOrderResponse)
                .toList();

        return new PaginatedOrderResponse(ordersPage, items);
    }

    public OrderResponse getOrderById(int orderId) {
        SaleOrder order = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return buildOrderResponse(order);
    }

    @Transactional
    public void deleteOrder(int orderId) {
        SaleOrder order = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setIsActive(false);
        saleOrderRepository.save(order);
    }

    public ShippingDetailsResponse getShippingDetails(int orderId) {
        saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping details not found for order: " + orderId));

        List<OrderTrackingLog> logs = orderTrackingLogRepository
                .findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId());

        return shippingDetailsMapper.toResponse(tracking, logs);
    }

    @Transactional
    public ShippingDetailsResponse updateShippingStatus(int orderId, ShippingStatusUpdateRequest req) {
        saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping details not found for order: " + orderId));

        DeliveryStatus newStatus = deliveryStatusRepository.findById(req.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery status not found with id: " + req.getStatusId()));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser changedBy = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));

        DeliveryStatus previousStatus = tracking.getStatus();

        log.info("Updating shipping status for order={}", orderId);

        if (previousStatus != null && newStatus.getStepOrder() != null && previousStatus.getStepOrder() != null
                && newStatus.getStepOrder() < previousStatus.getStepOrder()) {
            throw new BadRequestException("Delivery status update cannot go backwards");
        }

        if(previousStatus.getId() == newStatus.getId()) {
            throw new BadRequestException("Delivery status update cannot be the same as the previous status");
        }

        orderTrackingLogRepository.save(new OrderTrackingLog(tracking, previousStatus, newStatus, changedBy));
        log.info("Logging shipping status change");

        tracking.setStatus(newStatus);
        tracking.setUpdatedAt(LocalDateTime.now());
        deliveryTrackingRepository.save(tracking);

        applicationEventPublisher.publishEvent(
                new DeliveryStatusChangedEvent(tracking, previousStatus, newStatus)
        );

        List<OrderTrackingLog> logs = orderTrackingLogRepository
                .findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId());
        log.info("Order status updated to {}", newStatus.getName() + " for order " + orderId);
        return shippingDetailsMapper.toResponse(tracking, logs);
    }
    
    public List<DeliveryStatusResponse> getDeliveryStatuses() {
        return deliveryStatusRepository.findAllByOrderByStepOrder()
                .stream()
                .map(deliveryStatusMapper::toResponse)
                .toList();
    }

    public OrderResponse buildOrderResponse(SaleOrder order) {
        Optional<OrderBill> bill = orderBillRepository.findByOrderId(order.getId());
        Optional<DeliveryTracking> trackingOpt = deliveryTrackingRepository.findByOrderId(order.getId());
        Optional<StripePayment> payment = stripePaymentRepository.findByOrderId(order.getId());
        List<OrderDetails> orderDetails = orderDetailsRepository.findByOrderId(order.getId());

        ClientAddress address = trackingOpt.map(DeliveryTracking::getAddress).orElse(null);
        DeliveryTracking tracking = trackingOpt.orElse(null);

        return orderMapper.toResponse(order, bill.orElse(null), payment.orElse(null), address, tracking, orderDetails);
    }

    private String resolveSortField(String sortBy) {
        if (sortBy == null) return "orderDate";
        return switch (sortBy.toLowerCase()) {
            case "date", "order_date" -> "orderDate";
            case "id" -> "id";
            default -> "orderDate";
        };
    }

    private LocalDateTime parseDateParam(String dateStr, boolean endOfDay) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (DateTimeParseException e) {
            log.warn("Invalid date parameter: {}", dateStr);
            return null;
        }
    }
}
