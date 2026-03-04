package co.ravn.ecommerce.services.cart;

import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.entities.cart.ShoppingCart;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.cart.ShoppingCartRepository;
import co.ravn.ecommerce.utils.enums.ShoppingCartStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cartAuthorization")
@AllArgsConstructor
@Slf4j
public class CartAuthorization {
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    public boolean isCartOwner(int cartId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.info("A user has not been provided for the shopping cart");
            return false;
        }

        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found with id: " + cartId));

        String username = authentication.getName();
        Optional<SysUser> user = userRepository.findByUsernameAndIsActiveTrue(username);

        if (user.isEmpty()) {
            log.info("User with username " + username + " not found or is inactive");
            return false;
        }

        boolean isOwner = user.get().getPerson() != null && user.get().getPerson().getId() == cart.getClient().getId();
        log.info("User " + user.get().getId() + " " + username + (isOwner ? " is " : " isn't ") + "the owner");
        return isOwner;
    }
}
