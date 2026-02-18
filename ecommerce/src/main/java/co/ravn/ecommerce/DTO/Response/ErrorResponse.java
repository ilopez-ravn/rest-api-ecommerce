package co.ravn.ecommerce.DTO.Response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ErrorResponse {
    private final String error_code;
    private final String message;

    public ErrorResponse(String error_code, String message) {
        this.error_code = error_code;
        this.message = message;
    }

}
