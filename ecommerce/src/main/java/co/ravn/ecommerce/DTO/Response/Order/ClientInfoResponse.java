package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ClientInfoResponse {
    private int id;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private String document;
    private String document_type;
    private LocalDateTime created_at;
}
