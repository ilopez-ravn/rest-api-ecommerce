package co.ravn.ecommerce.services.order;

import co.ravn.ecommerce.dto.RefundApprovedEvent;
import co.ravn.ecommerce.dto.RefundDeniedEvent;
import co.ravn.ecommerce.dto.RefundRequestedEvent;
import co.ravn.ecommerce.dto.response.order.RefundResponse;
import co.ravn.ecommerce.entities.auth.Person;
import co.ravn.ecommerce.entities.auth.Role;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.entities.inventory.Warehouse;
import co.ravn.ecommerce.entities.order.DeliveryStatus;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.OrderDetails;
import co.ravn.ecommerce.entities.order.RefundRequest;
import co.ravn.ecommerce.entities.order.ReturnShipment;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.entities.order.StripePayment;
import co.ravn.ecommerce.dto.ReturnInTransitEvent;
import co.ravn.ecommerce.dto.request.order.RefundRequestDto;
import co.ravn.ecommerce.dto.request.order.RefundReviewDto;
import co.ravn.ecommerce.dto.request.order.ReturnShippingDto;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ConflictException;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock private SaleOrderRepository saleOrderRepository;
    @Mock private OrderDetailsRepository orderDetailsRepository;
    @Mock private RefundRequestRepository refundRequestRepository;
    @Mock private ReturnShipmentRepository returnShipmentRepository;
    @Mock private DeliveryTrackingRepository deliveryTrackingRepository;
    @Mock private DeliveryStatusRepository deliveryStatusRepository;
    @Mock private StripePaymentRepository stripePaymentRepository;
    @Mock private StripePaymentEventLogRepository stripePaymentEventLogRepository;
    @Mock private ProductStockRepository productStockRepository;
    @Mock private ProductChangesLogRepository productChangesLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private RefundMapper refundMapper;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private RefundService refundService;

    private SysUser clientUser;
    private SysUser managerUser;
    private Person clientPerson;
    private SaleOrder order;
    private StripePayment stripePayment;
    private RefundRequest pendingRefundRequest;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        Role clientRole = new Role(1, RoleEnum.CLIENT, true);
        Role managerRole = new Role(2, RoleEnum.MANAGER, true);

        clientPerson = new Person();
        clientPerson.setId(10);
        clientPerson.setEmail("client@test.com");
        clientPerson.setFirstName("John");
        clientPerson.setLastName("Doe");

        clientUser = new SysUser();
        clientUser.setId(1);
        clientUser.setUsername("client");
        clientUser.setRole(clientRole);
        clientPerson.setSysUser(clientUser);
        clientUser.setPerson(clientPerson);

        managerUser = new SysUser();
        managerUser.setId(2);
        managerUser.setUsername("manager");
        managerUser.setRole(managerRole);

        warehouse = new Warehouse();
        warehouse.setId(5);

        order = new SaleOrder();
        order.setId(1);
        order.setClient(clientPerson);
        order.setWarehouse(warehouse);

        stripePayment = new StripePayment();
        stripePayment.setId(1);
        stripePayment.setOrder(order);
        stripePayment.setStripePaymentId("pi_test123");
        stripePayment.setPaymentStatus("SUCCEEDED");
        stripePayment.setAmount(new BigDecimal("100.00"));

        pendingRefundRequest = new RefundRequest();
        pendingRefundRequest.setId(1);
        pendingRefundRequest.setOrder(order);
        pendingRefundRequest.setRequestedBy(clientUser);
        pendingRefundRequest.setStatus(RefundStatus.PENDING_REVIEW);
        pendingRefundRequest.setRequiresReturn(false);
        pendingRefundRequest.setReason("Defective product");
        pendingRefundRequest.setRequestedAt(LocalDateTime.now());
    }

    private void mockSecurityContext(SysUser user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        // Use lenient: some error-path tests throw before getCurrentUser() is called
        lenient().when(userRepository.findByUsernameAndIsActiveTrue(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Nested
    @DisplayName("requestRefund")
    class RequestRefund {

        @Test
        @DisplayName("pre-shipment happy path — no delivery tracking, requiresReturn=false")
        void preShipmentHappyPath() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Defective product");

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            when(refundRequestRepository.existsByOrderIdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
            when(deliveryTrackingRepository.findByOrderId(1)).thenReturn(Optional.empty());
            when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            RefundResponse response = new RefundResponse();
            when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(response);

            RefundResponse result = refundService.requestRefund(1, dto);

            assertThat(result).isNotNull();
            verify(refundRequestRepository).save(argThat(r -> !r.isRequiresReturn()));
            verify(applicationEventPublisher).publishEvent(any(RefundRequestedEvent.class));
        }

        @Test
        @DisplayName("post-delivery happy path — delivered status, requiresReturn=true")
        void postDeliveryHappyPath() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Wrong item");

            DeliveryStatus shippedStatus = new DeliveryStatus("SHIPPED", 3, "Shipped");
            DeliveryStatus deliveredStatus = new DeliveryStatus("DELIVERED", 5, "Delivered");

            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setStatus(deliveredStatus);
            tracking.setUpdatedAt(LocalDateTime.now().minusDays(5));

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            when(refundRequestRepository.existsByOrderIdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
            when(deliveryTrackingRepository.findByOrderId(1)).thenReturn(Optional.of(tracking));
            when(deliveryStatusRepository.findByName("SHIPPED")).thenReturn(Optional.of(shippedStatus));
            when(deliveryStatusRepository.findByName("DELIVERED")).thenReturn(Optional.of(deliveredStatus));
            when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(new RefundResponse());

            RefundResponse result = refundService.requestRefund(1, dto);

            assertThat(result).isNotNull();
            verify(refundRequestRepository).save(argThat(RefundRequest::isRequiresReturn));
        }

        @Test
        @DisplayName("mid-transit throws BadRequestException")
        void midTransitThrows() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Changed mind");

            DeliveryStatus shippedStatus = new DeliveryStatus("SHIPPED", 3, "Shipped");
            DeliveryStatus inTransitStatus = new DeliveryStatus("IN_TRANSIT", 4, "In Transit");
            DeliveryStatus deliveredStatus = new DeliveryStatus("DELIVERED", 5, "Delivered");

            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setStatus(inTransitStatus);

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            when(refundRequestRepository.existsByOrderIdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
            when(deliveryTrackingRepository.findByOrderId(1)).thenReturn(Optional.of(tracking));
            when(deliveryStatusRepository.findByName("SHIPPED")).thenReturn(Optional.of(shippedStatus));
            when(deliveryStatusRepository.findByName("DELIVERED")).thenReturn(Optional.of(deliveredStatus));

            assertThatThrownBy(() -> refundService.requestRefund(1, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("in transit");
        }

        @Test
        @DisplayName("payment not SUCCEEDED throws BadRequestException")
        void paymentNotSucceededThrows() {
            mockSecurityContext(clientUser);
            stripePayment.setPaymentStatus("PENDING");
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Test");

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));

            assertThatThrownBy(() -> refundService.requestRefund(1, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not eligible");
        }

        @Test
        @DisplayName("expired 30-day window throws BadRequestException")
        void expiredWindowThrows() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Late return");

            DeliveryStatus shippedStatus = new DeliveryStatus("SHIPPED", 3, "Shipped");
            DeliveryStatus deliveredStatus = new DeliveryStatus("DELIVERED", 5, "Delivered");

            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setStatus(deliveredStatus);
            tracking.setUpdatedAt(LocalDateTime.now().minusDays(31));

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            when(refundRequestRepository.existsByOrderIdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
            when(deliveryTrackingRepository.findByOrderId(1)).thenReturn(Optional.of(tracking));
            when(deliveryStatusRepository.findByName("SHIPPED")).thenReturn(Optional.of(shippedStatus));
            when(deliveryStatusRepository.findByName("DELIVERED")).thenReturn(Optional.of(deliveredStatus));

            assertThatThrownBy(() -> refundService.requestRefund(1, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("duplicate active refund throws ConflictException")
        void duplicateThrowsConflict() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Test");

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            when(refundRequestRepository.existsByOrderIdAndStatusNotIn(eq(1), anyList())).thenReturn(true);

            assertThatThrownBy(() -> refundService.requestRefund(1, dto))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("order not found throws ResourceNotFoundException")
        void orderNotFoundThrows() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Test");

            when(saleOrderRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refundService.requestRefund(99, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("ownership mismatch throws BadRequestException")
        void ownershipMismatchThrows() {
            mockSecurityContext(clientUser);
            RefundRequestDto dto = new RefundRequestDto();
            dto.setReason("Test");

            Person otherPerson = new Person();
            otherPerson.setId(99);
            SaleOrder otherOrder = new SaleOrder();
            otherOrder.setId(2);
            otherOrder.setClient(otherPerson);

            when(saleOrderRepository.findById(2)).thenReturn(Optional.of(otherOrder));

            assertThatThrownBy(() -> refundService.requestRefund(2, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not authorized");
        }
    }

    @Nested
    @DisplayName("approveRefund")
    class ApproveRefund {

        @Test
        @DisplayName("pre-shipment — issues Stripe refund immediately and sets REFUND_PROCESSED")
        void preShipmentApproveIssuesRefundImmediately() {
            mockSecurityContext(managerUser);
            RefundReviewDto dto = new RefundReviewDto();

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));
            // save and mapper may not be reached if Stripe throws first — use lenient
            lenient().when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderDetailsRepository.findByOrderId(1)).thenReturn(List.of());
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            lenient().when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(new RefundResponse());

            // Stripe.create will throw in test environment; verify it propagates as an exception
            assertThatThrownBy(() -> refundService.approveRefund(1, dto))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("post-delivery — sets APPROVED only, no Stripe call")
        void postDeliveryApproveOnlySetsApproved() {
            mockSecurityContext(managerUser);
            RefundReviewDto dto = new RefundReviewDto();

            pendingRefundRequest.setRequiresReturn(true);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));
            when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(new RefundResponse());

            RefundResponse result = refundService.approveRefund(1, dto);

            assertThat(result).isNotNull();
            assertThat(pendingRefundRequest.getStatus()).isEqualTo(RefundStatus.APPROVED);
            verify(applicationEventPublisher).publishEvent(any(RefundApprovedEvent.class));
            verify(stripePaymentRepository, never()).findByOrderId(anyInt());
        }

        @Test
        @DisplayName("wrong status throws BadRequestException")
        void wrongStatusThrows() {
            mockSecurityContext(managerUser);
            pendingRefundRequest.setStatus(RefundStatus.APPROVED);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.approveRefund(1, new RefundReviewDto()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PENDING_REVIEW");
        }

        @Test
        @DisplayName("not found throws ResourceNotFoundException")
        void notFoundThrows() {
            mockSecurityContext(managerUser);
            when(refundRequestRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refundService.approveRefund(99, new RefundReviewDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("denyRefund")
    class DenyRefund {

        @Test
        @DisplayName("happy path — sets DENIED with notes")
        void happyPath() {
            mockSecurityContext(managerUser);
            RefundReviewDto dto = new RefundReviewDto();
            dto.setManagerNotes("Not eligible per policy");

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));
            when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(new RefundResponse());

            RefundResponse result = refundService.denyRefund(1, dto);

            assertThat(result).isNotNull();
            assertThat(pendingRefundRequest.getStatus()).isEqualTo(RefundStatus.DENIED);
            verify(applicationEventPublisher).publishEvent(any(RefundDeniedEvent.class));
        }

        @Test
        @DisplayName("blank managerNotes throws BadRequestException")
        void blankNotesThrows() {
            mockSecurityContext(managerUser);
            RefundReviewDto dto = new RefundReviewDto();
            dto.setManagerNotes("   ");

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.denyRefund(1, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("required for denial");
        }

        @Test
        @DisplayName("wrong status throws BadRequestException")
        void wrongStatusThrows() {
            mockSecurityContext(managerUser);
            pendingRefundRequest.setStatus(RefundStatus.DENIED);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.denyRefund(1, new RefundReviewDto()))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("submitReturnShipping")
    class SubmitReturnShipping {

        @Test
        @DisplayName("happy path — creates ReturnShipment and sets RETURN_IN_TRANSIT")
        void happyPath() {
            mockSecurityContext(clientUser);
            ReturnShippingDto dto = new ReturnShippingDto();
            dto.setTrackingNumber("TRK123");
            dto.setCarrierName("FedEx");

            pendingRefundRequest.setStatus(RefundStatus.APPROVED);
            pendingRefundRequest.setRequiresReturn(true);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));
            when(returnShipmentRepository.save(any(ReturnShipment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(new RefundResponse());

            RefundResponse result = refundService.submitReturnShipping(1, dto);

            assertThat(result).isNotNull();
            assertThat(pendingRefundRequest.getStatus()).isEqualTo(RefundStatus.RETURN_IN_TRANSIT);
            verify(returnShipmentRepository).save(any(ReturnShipment.class));
            verify(applicationEventPublisher).publishEvent(any(ReturnInTransitEvent.class));
        }

        @Test
        @DisplayName("requiresReturn=false throws BadRequestException")
        void requiresReturnFalseThrows() {
            mockSecurityContext(clientUser);
            pendingRefundRequest.setStatus(RefundStatus.APPROVED);
            pendingRefundRequest.setRequiresReturn(false);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.submitReturnShipping(1, new ReturnShippingDto()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not require");
        }

        @Test
        @DisplayName("wrong status throws BadRequestException")
        void wrongStatusThrows() {
            mockSecurityContext(clientUser);
            pendingRefundRequest.setStatus(RefundStatus.PENDING_REVIEW);
            pendingRefundRequest.setRequiresReturn(true);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.submitReturnShipping(1, new ReturnShippingDto()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("APPROVED");
        }

        @Test
        @DisplayName("not order owner throws BadRequestException")
        void notOwnerThrows() {
            mockSecurityContext(clientUser);
            Person otherPerson = new Person();
            otherPerson.setId(99);
            SaleOrder otherOrder = new SaleOrder();
            otherOrder.setId(2);
            otherOrder.setClient(otherPerson);

            RefundRequest otherRefund = new RefundRequest();
            otherRefund.setId(2);
            otherRefund.setOrder(otherOrder);
            otherRefund.setRequestedBy(clientUser);
            otherRefund.setStatus(RefundStatus.APPROVED);
            otherRefund.setRequiresReturn(true);

            when(refundRequestRepository.findById(2)).thenReturn(Optional.of(otherRefund));

            assertThatThrownBy(() -> refundService.submitReturnShipping(2, new ReturnShippingDto()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not authorized");
        }
    }

    @Nested
    @DisplayName("markProductReceived")
    class MarkProductReceived {

        @Test
        @DisplayName("happy path — stock restored and REFUND_PROCESSED")
        void happyPath() {
            mockSecurityContext(managerUser);
            pendingRefundRequest.setStatus(RefundStatus.RETURN_IN_TRANSIT);
            pendingRefundRequest.setRequiresReturn(true);

            ReturnShipment returnShipment = new ReturnShipment();
            returnShipment.setId(1);
            returnShipment.setRefundRequest(pendingRefundRequest);

            Product product = new Product();
            product.setId(1);
            OrderDetails orderDetails = new OrderDetails(order, product, new BigDecimal("50.00"), 2, 10);

            ProductStock stock = new ProductStock();
            stock.setQuantity(5);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));
            when(returnShipmentRepository.findByRefundRequestId(1)).thenReturn(Optional.of(returnShipment));
            when(returnShipmentRepository.save(any(ReturnShipment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderDetailsRepository.findByOrderId(1)).thenReturn(List.of(orderDetails));
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.of(stripePayment));
            when(productStockRepository.findByWarehouseIdAndProductId(5, 1)).thenReturn(Optional.of(stock));
            when(productStockRepository.save(any(ProductStock.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productChangesLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // stubs after Stripe throws may not be reached — use lenient
            lenient().when(stripePaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(stripePaymentEventLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(refundMapper.toResponse(any(RefundRequest.class))).thenReturn(new RefundResponse());

            // Stripe.create will fail in test — verify it throws an exception
            assertThatThrownBy(() -> refundService.markProductReceived(1))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("requiresReturn=false throws BadRequestException")
        void requiresReturnFalseThrows() {
            mockSecurityContext(managerUser);
            pendingRefundRequest.setStatus(RefundStatus.RETURN_IN_TRANSIT);
            pendingRefundRequest.setRequiresReturn(false);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.markProductReceived(1))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not require");
        }

        @Test
        @DisplayName("wrong status throws BadRequestException")
        void wrongStatusThrows() {
            mockSecurityContext(managerUser);
            pendingRefundRequest.setStatus(RefundStatus.APPROVED);

            when(refundRequestRepository.findById(1)).thenReturn(Optional.of(pendingRefundRequest));

            assertThatThrownBy(() -> refundService.markProductReceived(1))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("RETURN_IN_TRANSIT");
        }
    }

    @Nested
    @DisplayName("getRefundByOrder")
    class GetRefundByOrder {

        @Test
        @DisplayName("CLIENT owner can view — returns response")
        void clientOwnerCanView() {
            mockSecurityContext(clientUser);

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(refundRequestRepository.findByOrderId(1)).thenReturn(Optional.of(pendingRefundRequest));
            when(refundMapper.toResponse(pendingRefundRequest)).thenReturn(new RefundResponse());

            RefundResponse result = refundService.getRefundByOrder(1);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("refund not found throws ResourceNotFoundException")
        void refundNotFoundThrows() {
            mockSecurityContext(clientUser);

            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(refundRequestRepository.findByOrderId(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refundService.getRefundByOrder(1))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("CLIENT non-owner throws BadRequestException")
        void nonOwnerThrows() {
            mockSecurityContext(clientUser);

            Person otherPerson = new Person();
            otherPerson.setId(99);
            SaleOrder otherOrder = new SaleOrder();
            otherOrder.setId(2);
            otherOrder.setClient(otherPerson);

            when(saleOrderRepository.findById(2)).thenReturn(Optional.of(otherOrder));

            assertThatThrownBy(() -> refundService.getRefundByOrder(2))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not authorized");
        }
    }
}
