package co.ravn.ecommerce.Services.Cart;

import co.ravn.ecommerce.Annotations.CartOwner;
import co.ravn.ecommerce.DTO.Request.Cart.CartProductRequest;
import co.ravn.ecommerce.DTO.Request.Cart.NewCartRequest;
import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Exception.BadRequestException;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Cart.CartMapper;
import co.ravn.ecommerce.Repositories.Auth.PersonRepository;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Utils.enums.RoleEnum;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ShoppingCartResponse createCart(NewCartRequest newCartRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));

        Optional<ShoppingCart> existingCartOpt = shoppingCartRepository.findByClientIdAndStatus(
                loggedInUser.getId(),
                ShoppingCartStatusEnum.ACTIVE);

        ShoppingCart cart;
        if (existingCartOpt.isPresent()) {
            cart = existingCartOpt.get();
        } else {
            cart = new ShoppingCart(loggedInUser.getPerson(), ShoppingCartStatusEnum.ACTIVE);
        }

        List<ShoppingCartDetails> shoppingCartDetails = newCartRequest.getProducts().stream()
                .map(product -> ShoppingCartDetails.builder()
                        .cart(cart)
                        .product(productRepository
                                .findByIdAndIsActiveTrueAndDeletedAtIsNull(product.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + product.getProductId())))
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .build())
                .collect(Collectors.toList());

        cart.setProducts(shoppingCartDetails);
        shoppingCartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    public ShoppingCartResponse getCartById(int id) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));
        return cartMapper.toResponse(cart);
    }

    @CartOwner
    @Transactional
    public ShoppingCartResponse addItemToCart(int id, CartProductRequest cartProductRequest) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));

        log.info("Adding product with id {} to cart with id {}", cartProductRequest.getProductId(), id);

        Product product = productRepository
                .findByIdAndIsActiveTrueAndDeletedAtIsNull(cartProductRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + cartProductRequest.getProductId()));

        log.info("Product with id {} found: {}", cartProductRequest.getProductId(), product.getName());

        // Validate available stock across all warehouses for this product
        List<ProductStock> stocks = product.getStock();
        int totalStock = stocks.stream().mapToInt(ProductStock::getQuantity).sum();
        int quantityAlreadyInCart = cart.getProducts().stream()
                .filter(d -> d.getProduct().getId() == product.getId())
                .mapToInt(ShoppingCartDetails::getQuantity)
                .sum();
        int requestedQuantity = cartProductRequest.getQuantity();

        if (totalStock <= 0 || quantityAlreadyInCart + requestedQuantity > totalStock) {
            throw new BadRequestException("Insufficient stock for product id: " + product.getId());
        }

        BigDecimal price = (cartProductRequest.getPrice() != null) ? cartProductRequest.getPrice() : product.getPrice();

        // check if product already exists in cart and update the quantity
        ShoppingCartDetails existingItem = cart.getProducts().stream()
                .filter(d -> d.getProduct().getId() == product.getId())
                .findFirst()
                .orElse(null);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + cartProductRequest.getQuantity());
            existingItem.setPrice(price);
            existingItem.setUpdatedAt(LocalDateTime.now());
            shoppingCartRepository.save(cart);
            return cartMapper.toResponse(cart);
        }

       cart.getProducts().add(ShoppingCartDetails.builder()
                .cart(cart)
                .product(product)
                .price(price)
                .quantity(cartProductRequest.getQuantity())
                .build());
        shoppingCartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @CartOwner
    @Transactional
    public ShoppingCartResponse removeItemFromCart(int id, int itemId) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));

        cart.getProducts().removeIf(item -> item.getId() == itemId);
        shoppingCartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    public ShoppingCartResponse getCartByClientId(int personId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + auth.getName()));

        if (!loggedInUser.getRole().getName().toString().equals(RoleEnum.MANAGER.toString())
                && loggedInUser.getPerson().getId() != personId) {
            throw new AccessDeniedException("You do not own this cart");
        }

        Optional<ShoppingCart> cart = shoppingCartRepository.findByClientIdAndStatus(personId, ShoppingCartStatusEnum.ACTIVE);

        if (cart.isEmpty()) {
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
            ShoppingCart newCart = new ShoppingCart(person, ShoppingCartStatusEnum.ACTIVE);
            newCart.setProducts(Collections.emptyList());
            shoppingCartRepository.save(newCart);
            return new ShoppingCartResponse(newCart, cartMapper);
        }

        return new ShoppingCartResponse(cart.get(), cartMapper);
    }

    @CartOwner
    @Transactional
    public ShoppingCartResponse updateCartItem(int id, int itemId, CartProductRequest cartProductRequest) {
        ShoppingCart cart = shoppingCartRepository.findByIdAndStatus(id, ShoppingCartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for client id: " + id));

        ShoppingCartDetails item = cart.getProducts().stream()
                .filter(i -> i.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        Product product = item.getProduct();

        // Validate available stock across all warehouses for this product
        List<ProductStock> stocks = product.getStock();
        int totalStock = stocks.stream().mapToInt(ProductStock::getQuantity).sum();

        int quantityInOtherItems = cart.getProducts().stream()
                .filter(d -> d.getProduct().getId() == product.getId() && d.getId() != itemId)
                .mapToInt(ShoppingCartDetails::getQuantity)
                .sum();
        int newQuantityForItem = cartProductRequest.getQuantity();

        if (totalStock <= 0 || quantityInOtherItems + newQuantityForItem > totalStock) {
            throw new BadRequestException("Insufficient stock for product id: " + product.getId());
        }

        BigDecimal price = (cartProductRequest.getPrice() != null) ? cartProductRequest.getPrice() : product.getPrice();

        item.setPrice(price);
        item.setQuantity(newQuantityForItem);
        shoppingCartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }
}
