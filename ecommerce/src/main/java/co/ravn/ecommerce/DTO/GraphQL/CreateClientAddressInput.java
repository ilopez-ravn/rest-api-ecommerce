package co.ravn.ecommerce.DTO.GraphQL;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateClientAddressInput {

    private String address_line1;
    private String address_line2;
    private String city;
    private String state;
    private String postal_code;
    private String country;
}

