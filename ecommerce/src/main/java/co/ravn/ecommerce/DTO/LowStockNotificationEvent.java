package co.ravn.ecommerce.DTO;

public record LowStockNotificationEvent(
        int productId,
        String productName,
        int remainingStock) {
}
