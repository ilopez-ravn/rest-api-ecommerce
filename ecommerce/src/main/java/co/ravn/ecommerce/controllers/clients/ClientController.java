package co.ravn.ecommerce.controllers.clients;

import co.ravn.ecommerce.dto.request.clients.NewClientAddressRequest;
import co.ravn.ecommerce.dto.request.clients.UpdateClientAddressRequest;
import co.ravn.ecommerce.dto.response.order.AddressResponse;
import co.ravn.ecommerce.services.clients.ClientAddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/clients")
@AllArgsConstructor
@Validated
public class ClientController {

    private final ClientAddressService clientAddressService;

    @GetMapping("/{clientId}/addresses")
    public ResponseEntity<List<AddressResponse>> getAddressesForClient(
            @PathVariable @Min(1) int clientId) {
        return ResponseEntity.ok(clientAddressService.getAddressesForClient(clientId));
    }

    @PostMapping("/{clientId}/addresses")
    public ResponseEntity<AddressResponse> createAddressForClient(
            @PathVariable @Min(1) int clientId,
            @RequestBody @Valid NewClientAddressRequest request) {
        return ResponseEntity.ok(clientAddressService.createAddressForClient(clientId, request));
    }

    @PutMapping("/{clientId}/addresses/{id}")
    public ResponseEntity<AddressResponse> updateAddressForClient(
            @PathVariable @Min(1) int clientId,
            @PathVariable @Min(1) int id,
            @RequestBody @Valid UpdateClientAddressRequest request) {
        return ResponseEntity.ok(clientAddressService.updateAddressForClient(clientId, id, request));
    }

    @DeleteMapping("/{clientId}/addresses/{id}")
    public ResponseEntity<Void> deleteAddressForClient(
            @PathVariable @Min(1) int clientId,
            @PathVariable @Min(1) int id) {
        clientAddressService.deleteAddressForClient(clientId, id);
        return ResponseEntity.noContent().build();
    }
}
