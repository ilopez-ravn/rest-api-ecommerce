package co.ravn.ecommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefundStatusUpdateDto {

    @NotBlank
    @Pattern(regexp = "APPROVE|DENY", message = "action must be APPROVE or DENY")
    private String action;

    @Size(max = 500)
    private String managerNotes;
}
