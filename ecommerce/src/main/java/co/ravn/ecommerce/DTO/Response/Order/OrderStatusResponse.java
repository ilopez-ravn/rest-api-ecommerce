package co.ravn.ecommerce.DTO.Response.Order;

import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderStatusResponse {
    private int id;
    private boolean isActive;
    private LocalDateTime orderDate;
    private PaymentInfoResponse payment_info;

    public OrderStatusResponse(SaleOrder order, StripePayment payment) {
        this.id = order.getId();
        this.isActive = order.getIsActive();
        this.orderDate = order.getOrderDate();
        this.payment_info = new PaymentInfoResponse(
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod()
        );
    }
}
