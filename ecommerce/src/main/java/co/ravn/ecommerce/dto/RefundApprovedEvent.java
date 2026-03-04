package co.ravn.ecommerce.dto;

import co.ravn.ecommerce.entities.order.RefundRequest;

public record RefundApprovedEvent(RefundRequest refundRequest) {}
