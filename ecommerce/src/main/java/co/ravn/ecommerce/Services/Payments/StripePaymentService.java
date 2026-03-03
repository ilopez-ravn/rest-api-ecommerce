package co.ravn.ecommerce.Services.Payments;

import co.ravn.ecommerce.Config.StripeConfig;
import co.ravn.ecommerce.DTO.Request.Payment.PaymentIntentRequest;
import co.ravn.ecommerce.DTO.Response.Order.OrderStatusResponse;
import co.ravn.ecommerce.DTO.Response.Payment.PaymentIntentResponse;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Clients.ClientAddress;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import co.ravn.ecommerce.Entities.Order.OrderBill;
import co.ravn.ecommerce.Entities.Order.OrderDetails;
import co.ravn.ecommerce.Entities.Order.OrderTrackingLog;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;
import co.ravn.ecommerce.Exception.BadRequestException;
import co.ravn.ecommerce.Exception.ConfigurationException;
import co.ravn.ecommerce.Exception.PaymentFailureException;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Clients.ClientAddressRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductStockRepository;
import co.ravn.ecommerce.Repositories.Inventory.WarehouseRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryStatusRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryTrackingRepository;
import co.ravn.ecommerce.Repositories.Order.OrderBillRepository;
import co.ravn.ecommerce.Repositories.Order.OrderDetailsRepository;
import co.ravn.ecommerce.Repositories.Order.OrderTrackingLogRepository;
import co.ravn.ecommerce.Repositories.Order.SaleOrderRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentRepository;
import co.ravn.ecommerce.Services.Order.OrderService;
import co.ravn.ecommerce.Utils.enums.BillDocumentTypeEnum;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class StripePaymentService {

    private final OrderService orderService;
    private final StripeConfig stripeConfig;
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartDetailsRepository shoppingCartDetailsRepository;
    private final ProductStockRepository productStockRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final StripePaymentRepository stripePaymentRepository;
    private final WarehouseRepository warehouseRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final OrderTrackingLogRepository orderTrackingLogRepository;
    private final OrderBillRepository orderBillRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    @Transactional
    public PaymentIntentResponse createOrRetrievePaymentIntent(PaymentIntentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + auth.getName()));

        ShoppingCart cart = shoppingCartRepository
                .findByIdAndStatus(request.getShoppingCartId(), ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active cart not found with id: " + request.getShoppingCartId()));

        if (currentUser.getPerson() == null || cart.getClient().getId() != currentUser.getPerson().getId()) {
            throw new AccessDeniedException("You do not own this cart");
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse not found with id: " + request.getWarehouseId()));

        ClientAddress address = clientAddressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + request.getAddressId()));
        if (address.getClient().getId() != currentUser.getPerson().getId()) {
            throw new AccessDeniedException("You do not own this address");
        }

        List<ShoppingCartDetails> items = shoppingCartDetailsRepository.findByCartId(request.getShoppingCartId());
        if (items.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // If an order already exists for this cart, return existing payment or create one
        Optional<SaleOrder> existingOrder = saleOrderRepository.findByShoppingCartId(request.getShoppingCartId());
        if (existingOrder.isPresent()) {
            Optional<StripePayment> existingPayment = stripePaymentRepository.findByOrderId(existingOrder.get().getId());
            if (existingPayment.isPresent()) {
                return new PaymentIntentResponse(existingPayment.get().getClientSecretKey());
            }
            // Order exists but no payment yet — create the payment intent
            return createPaymentIntentForOrder(existingOrder.get(), items, address, request.getDeliveryFee());
        }

        // Validate stock for all items in the specified warehouse
        for (ShoppingCartDetails item : items) {
            if (item.getProduct().getIsActive() == false || item.getProduct().getDeletedAt() != null) {
                throw new BadRequestException("Product is not active or deleted");
            }

            Optional<ProductStock> stockOpt = productStockRepository
                    .findByWarehouseIdAndProductId(warehouse.getId(), item.getProduct().getId());
            int available = stockOpt.get().getQuantity();
            log.info("Available stock for product: {} is: {}", item.getProduct().getName(), available);
            log.info("Required stock for product: {} is: {}", item.getProduct().getName(), item.getQuantity());
            if (available < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock in warehouse for product: "
                        + item.getProduct().getName()
                        + ". Available: " + available + ", required: " + item.getQuantity());
            }
        }

        log.info("Creating provisional SaleOrder for cart: {}", cart.getId());

        // Create provisional SaleOrder (isActive=false until payment succeeds)
        SaleOrder order = saleOrderRepository.save(new SaleOrder(currentUser.getPerson(), cart, warehouse, false));

        // Create DeliveryTracking with the provided address and initial status
        DeliveryStatus initialStatus = deliveryStatusRepository.findFirstByOrderByStepOrder()
                .orElseThrow(() -> new ConfigurationException("No delivery statuses configured"));

        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        DeliveryTracking tracking = deliveryTrackingRepository.save(
                new DeliveryTracking(order, address, null, null, initialStatus, trackingNumber, null));
        orderTrackingLogRepository.save(new OrderTrackingLog(tracking, null, initialStatus, null));

        return createPaymentIntentForOrder(order, items, address, request.getDeliveryFee());
    }

    private PaymentIntentResponse createPaymentIntentForOrder(SaleOrder order, List<ShoppingCartDetails> items,
                                                              ClientAddress address, BigDecimal deliveryFee) {
        BigDecimal itemsTotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        BigDecimal total = itemsTotal.add(fee);

        // Ensure order has OrderDetails and OrderBill
        if (orderBillRepository.findByOrderId(order.getId()).isEmpty()) {
            for (ShoppingCartDetails item : items) {
                orderDetailsRepository.save(
                        new OrderDetails(order, item.getProduct(), item.getPrice(), item.getQuantity(), 18));
            }
            String documentNumber = "BILL-" + order.getId() + "-" + System.currentTimeMillis();
            orderBillRepository.save(
                    new OrderBill(order, BillDocumentTypeEnum.RECEIPT, documentNumber, 18, itemsTotal, fee, true));
        }

        PaymentIntent intent;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(total.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(stripeConfig.getCurrency())
                    .addPaymentMethodType("card")
                    .putMetadata("sale_order_id", String.valueOf(order.getId()))
                    .build();
            intent = PaymentIntent.create(params);
        } catch (StripeException e) {
            log.error("Stripe PaymentIntent creation failed: {}", e.getMessage());
            throw new PaymentFailureException("Failed to create payment intent: " + e.getMessage(), e);
        }

        log.info("Stripe PaymentIntent created successfully: {}", intent.getId());
        log.info("Stripe PaymentIntent client secret: {}", intent.getClientSecret());

        StripePayment stripePayment = new StripePayment(
                order,
                intent.getId(),
                intent.getClientSecret(),
                "card",
                List.of("card"),
                total,
                stripeConfig.getCurrency(),
                "PENDING");
        stripePaymentRepository.save(stripePayment);

        return new PaymentIntentResponse(intent.getClientSecret());
    }

    public OrderStatusResponse getOrderStatusByShoppingCartId(int shoppingCartId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + auth.getName()));

        SaleOrder order = saleOrderRepository.findByShoppingCartId(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for cart id: " + shoppingCartId));

        if (order.getClient().getId() != currentUser.getPerson().getId()) {
            throw new AccessDeniedException("You do not own this order");
        }

        var paymentOpt = stripePaymentRepository.findByOrderId(order.getId());
        if (paymentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found for order id: " + order.getId());
        }

        OrderStatusResponse response = new OrderStatusResponse();
        response.setOrder(orderService.buildOrderResponse(order));

        if (order.getCancelledAt() != null || "REFUNDED".equals(paymentOpt.get().getPaymentStatus())) {
            response.setStatus("REFUNDED");
            response.setRefund_reason(order.getRefundReason());
        } else if ("SUCCEEDED".equals(paymentOpt.get().getPaymentStatus())) {
            response.setStatus("COMPLETED");
        } else {
            response.setStatus("PENDING");
        }

        return response;
    }
}
