package co.ravn.ecommerce.dto.request.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClientAddressRequest {

    @JsonProperty("address_line1")
    @NotBlank
    private String addressLine1;
    @JsonProperty("address_line2")
    private String addressLine2;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @JsonProperty("postal_code")
    @NotBlank
    private String postalCode;
    @NotBlank
    private String country;
}

