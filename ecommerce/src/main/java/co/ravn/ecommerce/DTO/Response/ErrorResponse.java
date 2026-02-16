package co.ravn.ecommerce.DTO.Response;

public class ErrorResponse {
    private final String error_code;
    private final String message;

    public ErrorResponse(String error_code, String message) {
        this.error_code = error_code;
        this.message = message;
    }

    public String getError_code() {
        return error_code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error_code='" + error_code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
