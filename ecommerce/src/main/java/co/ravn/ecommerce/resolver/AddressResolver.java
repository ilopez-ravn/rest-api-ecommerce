package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.graphql.CreateClientAddressInput;
import co.ravn.ecommerce.dto.graphql.UpdateClientAddressInput;
import co.ravn.ecommerce.dto.request.clients.NewClientAddressRequest;
import co.ravn.ecommerce.dto.request.clients.UpdateClientAddressRequest;
import co.ravn.ecommerce.dto.response.order.AddressResponse;
import co.ravn.ecommerce.services.clients.ClientAddressService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@AllArgsConstructor
public class AddressResolver {

    private final ClientAddressService clientAddressService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    public List<AddressResponse> addresses(@Argument int clientId) {
        return clientAddressService.getAddressesForClient(clientId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public AddressResponse createAddress(@Argument int clientId,
                                         @Argument("input") CreateClientAddressInput input) {
        NewClientAddressRequest req = new NewClientAddressRequest();
        req.setAddressLine1(input.getAddress_line1());
        req.setAddressLine2(input.getAddress_line2());
        req.setCity(input.getCity());
        req.setState(input.getState());
        req.setPostalCode(input.getPostal_code());
        req.setCountry(input.getCountry());
        return clientAddressService.createAddressForClient(clientId, req);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public AddressResponse updateAddress(@Argument int clientId,
                                         @Argument int id,
                                         @Argument("input") UpdateClientAddressInput input) {
        UpdateClientAddressRequest req = new UpdateClientAddressRequest();
        req.setAddressLine1(input.getAddressLine1());
        req.setAddressLine2(input.getAddressLine2());
        req.setCity(input.getCity());
        req.setState(input.getState());
        req.setPostalCode(input.getPostalCode());
        req.setCountry(input.getCountry());
        return clientAddressService.updateAddressForClient(clientId, id, req);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public Boolean deleteAddress(@Argument int clientId,
                                 @Argument int id) {
        clientAddressService.deleteAddressForClient(clientId, id);
        return true;
    }
}

