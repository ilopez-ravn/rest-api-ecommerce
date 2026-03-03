package co.ravn.ecommerce.DTO.Request.Clients;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewClientAddressRequest {

    @NotBlank
    @JsonProperty("address_line1")
    private String addressLine1;
    @JsonProperty("address_line2")
    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    @JsonProperty("postal_code")
    private String postalCode;

    @NotBlank
    private String country;
}

