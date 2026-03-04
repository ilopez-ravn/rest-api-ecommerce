package co.ravn.ecommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefundRequestDto {

    @NotBlank
    @Size(max = 1000)
    private String reason;
}
