package co.ravn.ecommerce.Models;

public class ErrorResponse {
    private final String error_code;
    private final String message;

    public ErrorResponse(String error_code, String message) {
        this.error_code = error_code;
        this.message = message;
    }


    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error_code='" + error_code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
