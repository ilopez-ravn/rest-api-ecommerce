package co.ravn.ecommerce.dto.response;

import lombok.Getter;

@Getter
public class MessageResponse {
    private final String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
