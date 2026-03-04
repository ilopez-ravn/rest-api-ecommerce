package co.ravn.ecommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReturnShippingDto {

    @NotBlank
    @Size(max = 100)
    private String trackingNumber;

    @NotBlank
    @Size(max = 100)
    private String carrierName;
}
