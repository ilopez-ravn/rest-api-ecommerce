package co.ravn.ecommerce.Services.Cart;

import co.ravn.ecommerce.DTO.Request.Cart.CartProductRequest;
import co.ravn.ecommerce.Annotations.CartOwner;
import co.ravn.ecommerce.DTO.Request.Cart.NewCartRequest;
import co.ravn.ecommerce.DTO.Response.ExceptionResponse;
import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Mappers.Cart.CartMapper;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Auth.PersonRepository;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Utils.enums.RoleEnum;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class CartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional
    public ResponseEntity<?> createCart(NewCartRequest newCartRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + auth.getName()));

        // Search if an active cart already exists for the user
        Optional<ShoppingCart> existingCartOpt = shoppingCartRepository.findByClientIdAndStatus(
                loggedInUser.getId(),
                ShoppingCartStatusEnum.ACTIVE);

        ShoppingCart cart;
        if (existingCartOpt.isPresent()) {
            // Overwrite the existing cart with the new products
            cart = existingCartOpt.get();
        } else {
            cart = new ShoppingCart(
                    loggedInUser.getPerson(),
                    ShoppingCartStatusEnum.ACTIVE);
        }
        List<ShoppingCartDetails> shoppingCartDetails = newCartRequest.getProducts().stream()
                .map(product -> new ShoppingCartDetails(cart, productRepository
                        .findByIdAndIsActiveTrueAndDeletedAtIsNull(product.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: "
                                + product.getProductId())),
                        product.getPrice(), product.getQuantity()))
                .collect(Collectors.toList());
        cart.setProducts(shoppingCartDetails);
        shoppingCartRepository.save(cart);

        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    public ResponseEntity<?> getCartById(int id) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));

        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    @CartOwner
    @Transactional
    public ResponseEntity<?> addItemToCart(int id, CartProductRequest cartProductRequest) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));

        log.info("Adding product with id {} to cart with id {}", cartProductRequest.getProductId(), id);

        Product product = productRepository
                .findByIdAndIsActiveTrueAndDeletedAtIsNull(cartProductRequest.getProductId())
                .orElseThrow(
                        () -> new RuntimeException("Product not found with id: "
                                + cartProductRequest.getProductId()));

        log.info("Product with id {} found: {}", cartProductRequest.getProductId(), product.getName());

        cart.getProducts().add(new ShoppingCartDetails(
                cart,
                product,
                cartProductRequest.getPrice(),
                cartProductRequest.getQuantity()));

        shoppingCartRepository.save(cart);
        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    @CartOwner
    @Transactional
    public ResponseEntity<?> removeItemFromCart(int id, int itemId) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));

        cart.getProducts().removeIf(item -> item.getId() == itemId);
        shoppingCartRepository.save(cart);

        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    public ResponseEntity<?> getCartByClientId(int personId) {
        Optional<ShoppingCart> cart = shoppingCartRepository.findByClientIdAndStatus(personId,
                ShoppingCartStatusEnum.ACTIVE);

        // Check if the id is for the current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + auth.getName()));
        
        if (!loggedInUser.getRole().getName().toString().equals(RoleEnum.MANAGER.toString()) && loggedInUser.getPerson().getId() != personId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ExceptionResponse(403, "Forbidden", "You do not own this cart", null));
        }

        if (cart.isEmpty()) {
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new RuntimeException(
                            "Person not found with id: " + personId));
            ShoppingCart newCart = new ShoppingCart(
                    person,
                    ShoppingCartStatusEnum.ACTIVE);
            newCart.setProducts(Collections.emptyList());
            shoppingCartRepository.save(newCart);
            return ResponseEntity.ok(new ShoppingCartResponse(newCart, cartMapper));
        }

        return ResponseEntity.ok(new ShoppingCartResponse(cart.get(), cartMapper));
    }

    @CartOwner
    @Transactional
    public ResponseEntity<?> updateCartItem(int id, int itemId, CartProductRequest cartProductRequest) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active cart not found for client id: " + id));

        Optional<ShoppingCartDetails> itemOpt = cart.getProducts().stream()
                .filter(item -> item.getId() == itemId)
                .findFirst();
        if (itemOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body("Cart item not found with id" + itemId);

        ShoppingCartDetails item = itemOpt.get();
        item.setPrice(cartProductRequest.getPrice());
        item.setQuantity(cartProductRequest.getQuantity());
        shoppingCartRepository.save(cart);

        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }
}
