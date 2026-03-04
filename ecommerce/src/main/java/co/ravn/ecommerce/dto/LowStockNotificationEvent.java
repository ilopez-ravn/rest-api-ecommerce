package co.ravn.ecommerce.dto;

public record LowStockNotificationEvent(
        int productId,
        String productName,
        int remainingStock) {
}
