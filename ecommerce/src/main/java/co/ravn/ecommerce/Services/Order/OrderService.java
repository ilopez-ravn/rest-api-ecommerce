package co.ravn.ecommerce.Services.Order;

import co.ravn.ecommerce.DTO.Request.Order.NewOrderRequest;
import co.ravn.ecommerce.DTO.Request.Order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Order.OrderResponse;
import co.ravn.ecommerce.DTO.Response.Order.PaginatedOrderResponse;
import co.ravn.ecommerce.Mappers.Order.OrderMapper;
import co.ravn.ecommerce.Mappers.Order.ShippingDetailsMapper;
import co.ravn.ecommerce.Entities.Clients.ClientAddress;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import co.ravn.ecommerce.Entities.Order.OrderBill;
import co.ravn.ecommerce.Entities.Order.OrderDetails;
import co.ravn.ecommerce.Entities.Order.OrderTrackingLog;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Exception.BadRequestException;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Clients.ClientAddressRepository;
import co.ravn.ecommerce.Repositories.Inventory.WarehouseRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryStatusRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryTrackingRepository;
import co.ravn.ecommerce.Repositories.Order.OrderBillRepository;
import co.ravn.ecommerce.Repositories.Order.OrderDetailsRepository;
import co.ravn.ecommerce.Repositories.Order.OrderTrackingLogRepository;
import co.ravn.ecommerce.Repositories.Order.OrderSpecs;
import co.ravn.ecommerce.Repositories.Order.SaleOrderRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Utils.enums.BillDocumentTypeEnum;
import co.ravn.ecommerce.Utils.enums.RoleEnum;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseRepository warehouseRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderBillRepository orderBillRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final OrderTrackingLogRepository orderTrackingLogRepository;
    private final StripePaymentRepository stripePaymentRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ShippingDetailsMapper shippingDetailsMapper;
    private final DeliveryStatusEmailService deliveryStatusEmailService;

    public ResponseEntity<?> getOrders(
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
                .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));

        // Non-managers can only see their own orders; ignore client_id from request
        if (!RoleEnum.MANAGER.toString().equals(currentUser.getRole().getName().toString())) {
            if (currentUser.getPerson() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
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

        return ResponseEntity.ok(new PaginatedOrderResponse(ordersPage, items));
    }

    @Transactional
    public ResponseEntity<?> createOrder(NewOrderRequest req) {
        ShoppingCart cart = shoppingCartRepository.findById(req.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + req.getCartId()));

        if (cart.getStatus() != ShoppingCartStatusEnum.ACTIVE) {
            return ResponseEntity.badRequest().body("Cart is not active");
        }

        Optional<SaleOrder> existingOrder = saleOrderRepository.findByShoppingCartId(req.getCartId());
        if (existingOrder.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("An order already exists for this cart");
        }

        Warehouse warehouse = warehouseRepository.findById(req.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + req.getWarehouseId()));

        ClientAddress address = clientAddressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + req.getAddressId()));

        SaleOrder order = saleOrderRepository.save(
                new SaleOrder(cart.getClient(), cart, warehouse, false)
        );

        List<ShoppingCartDetails> cartItems = cart.getProducts();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingCartDetails item : cartItems) {
            orderDetailsRepository.save(
                    new OrderDetails(order, item.getProduct(), item.getPrice(), item.getQuantity(), 18)
            );
            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        String documentNumber = "BILL-" + order.getId() + "-" + System.currentTimeMillis();
        OrderBill bill = orderBillRepository.save(
                new OrderBill(order, BillDocumentTypeEnum.RECEIPT, documentNumber, 18, totalAmount, BigDecimal.ZERO, true)
        );

        Optional<StripePayment> payment = stripePaymentRepository.findByOrderId(order.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toResponse(order, bill, payment.orElse(null)));
    }

    public ResponseEntity<?> getOrderById(int orderId) {
        SaleOrder order = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return ResponseEntity.ok(buildOrderResponse(order));
    }

    @Transactional
    public ResponseEntity<?> deleteOrder(int orderId) {
        SaleOrder order = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setIsActive(false);
        saleOrderRepository.save(order);

        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> getShippingDetails(int orderId) {
        saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping details not found for order: " + orderId));

        List<OrderTrackingLog> logs = orderTrackingLogRepository
                .findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId());

        return ResponseEntity.ok(shippingDetailsMapper.toResponse(tracking, logs));
    }

    @Transactional
    public ResponseEntity<?> updateShippingStatus(int orderId, ShippingStatusUpdateRequest req) {
        saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping details not found for order: " + orderId));

        DeliveryStatus newStatus = deliveryStatusRepository.findById(req.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery status not found with id: " + req.getStatusId()));


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser changedBy = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + auth.getName()));
        
                
        DeliveryStatus previousStatus = tracking.getStatus();

        log.info("Updating shipping status for order=" + orderId);

        if (previousStatus != null && newStatus.getStepOrder() != null && previousStatus.getStepOrder() != null
                && newStatus.getStepOrder() < previousStatus.getStepOrder()) {
            throw new BadRequestException("Delivery status update cannot go backwards");
        }

        orderTrackingLogRepository.save(
                new OrderTrackingLog(tracking, previousStatus, newStatus, changedBy)
        );

        log.info("Logging shipping status change");

        tracking.setStatus(newStatus);
        tracking.setUpdatedAt(LocalDateTime.now());
        deliveryTrackingRepository.save(tracking);

        deliveryStatusEmailService.sendDeliveryStatusUpdateEmail(tracking, previousStatus, newStatus);

        List<OrderTrackingLog> logs = orderTrackingLogRepository
                .findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId());

        return ResponseEntity.ok(shippingDetailsMapper.toResponse(tracking, logs));
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

