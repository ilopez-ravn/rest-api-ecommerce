package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TrackingLogResponse {
    private int id;
    private String previous_status;
    private String new_status;
    private Integer changed_by;
    private LocalDateTime changed_at;
}
