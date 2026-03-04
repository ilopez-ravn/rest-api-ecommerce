package co.ravn.ecommerce.dto.request.order;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefundReviewDto {

    @Size(max = 500)
    private String managerNotes;
}
