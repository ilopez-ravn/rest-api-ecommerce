package co.ravn.ecommerce.services.clients;

import co.ravn.ecommerce.dto.request.clients.NewClientAddressRequest;
import co.ravn.ecommerce.dto.request.clients.UpdateClientAddressRequest;
import co.ravn.ecommerce.dto.response.order.AddressResponse;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.clients.ClientAddress;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.order.AddressMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.clients.ClientAddressRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ClientAddressService {

    private final ClientAddressRepository clientAddressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    private SysUser getCurrentUserWithPerson() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsernameAndIsActiveTrueWithPerson(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));
    }

    private void assertOwnsAddress(SysUser currentUser, ClientAddress address) {
        if (currentUser.getPerson() == null
                || address.getClient() == null
                || currentUser.getPerson().getId() != address.getClient().getId()) {
            throw new AccessDeniedException("You are not allowed to modify this address");
        }
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesForClient(int clientId) {
        SysUser currentUser = getCurrentUserWithPerson();
        if (currentUser.getPerson() == null || currentUser.getPerson().getId() != clientId) {
            throw new AccessDeniedException("You are not allowed to access addresses for this client");
        }
        return clientAddressRepository.findByClientId(clientId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse createAddressForClient(int clientId, NewClientAddressRequest req) {
        SysUser currentUser = getCurrentUserWithPerson();
        if (currentUser.getPerson() == null || currentUser.getPerson().getId() != clientId) {
            throw new AccessDeniedException("You are not allowed to create addresses for this client");
        }

        var person = currentUser.getPerson();

        ClientAddress address = ClientAddress.builder()
                .client(person)
                .addressLine1(req.getAddressLine1())
                .addressLine2(req.getAddressLine2())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .country(req.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ClientAddress saved = clientAddressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddressForClient(int clientId, int id, UpdateClientAddressRequest req) {
        SysUser currentUser = getCurrentUserWithPerson();
        if (currentUser.getPerson() == null || currentUser.getPerson().getId() != clientId) {
            throw new AccessDeniedException("You are not allowed to update addresses for this client");
        }

        ClientAddress address = clientAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        assertOwnsAddress(currentUser, address);

        if (req.getAddressLine1() != null) address.setAddressLine1(req.getAddressLine1());
        if (req.getAddressLine2() != null) address.setAddressLine2(req.getAddressLine2());
        if (req.getCity() != null) address.setCity(req.getCity());
        if (req.getState() != null) address.setState(req.getState());
        if (req.getPostalCode() != null) address.setPostalCode(req.getPostalCode());
        if (req.getCountry() != null) address.setCountry(req.getCountry());
        address.setUpdatedAt(LocalDateTime.now());

        ClientAddress saved = clientAddressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAddressForClient(int clientId, int id) {
        SysUser currentUser = getCurrentUserWithPerson();
        if (currentUser.getPerson() == null || currentUser.getPerson().getId() != clientId) {
            throw new AccessDeniedException("You are not allowed to delete addresses for this client");
        }

        ClientAddress address = clientAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        assertOwnsAddress(currentUser, address);
        clientAddressRepository.delete(address);
    }
}

