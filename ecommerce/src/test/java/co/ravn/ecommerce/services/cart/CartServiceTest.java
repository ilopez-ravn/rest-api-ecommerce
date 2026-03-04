package co.ravn.ecommerce.services.cart;

import co.ravn.ecommerce.dto.request.cart.CartProductRequest;
import co.ravn.ecommerce.dto.request.cart.NewCartRequest;
import co.ravn.ecommerce.dto.response.cart.ShoppingCartResponse;
import co.ravn.ecommerce.entities.auth.Person;
import co.ravn.ecommerce.entities.auth.Role;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.cart.ShoppingCart;
import co.ravn.ecommerce.entities.cart.ShoppingCartDetails;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.cart.CartMapper;
import co.ravn.ecommerce.repositories.auth.PersonRepository;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.cart.ShoppingCartRepository;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.utils.enums.RoleEnum;
import co.ravn.ecommerce.utils.enums.ShoppingCartStatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getCartById")
    class GetCartById {

        @Test
        @DisplayName("returns cart response when found")
        void returnsResponseWhenFound() {
            ShoppingCart cart = new ShoppingCart();
            cart.setId(1);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);
            ShoppingCartResponse response = new ShoppingCartResponse();
            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(cartMapper.toResponse(cart)).thenReturn(response);

            ShoppingCartResponse result = cartService.getCartById(1);

            assertThat(result).isNotNull();
            verify(shoppingCartRepository).findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE);
            verify(cartMapper).toResponse(cart);
        }

        @Test
        @DisplayName("throws when cart not found")
        void throwsWhenNotFound() {
            when(shoppingCartRepository.findByIdAndStatus(99, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getCartById(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cart not found");
            verify(cartMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("createCart")
    class CreateCart {

        @Test
        @DisplayName("creates new cart when no existing active cart for the user")
        void createsNewCartWhenNoneExists() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("client1", null));
            Person person = new Person();
            person.setId(5);
            SysUser user = new SysUser();
            user.setId(10);
            user.setPerson(person);

            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            NewCartRequest request = new NewCartRequest(
                    List.of(new CartProductRequest(BigDecimal.TEN, 1, 1)));
            ShoppingCartResponse response = new ShoppingCartResponse();

            when(userRepository.findByUsernameAndIsActiveTrue("client1")).thenReturn(Optional.of(user));
            when(shoppingCartRepository.findByClientIdAndStatus(10, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(1))
                    .thenReturn(Optional.of(product));
            when(cartMapper.toResponse(any(ShoppingCart.class))).thenReturn(response);

            ShoppingCartResponse result = cartService.createCart(request);

            assertThat(result).isNotNull();
            verify(shoppingCartRepository).save(any(ShoppingCart.class));
        }

        @Test
        @DisplayName("reuses existing cart when one is already active for the user")
        void reusesExistingCartWhenPresent() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("client1", null));
            Person person = new Person();
            person.setId(5);
            SysUser user = new SysUser();
            user.setId(10);
            user.setPerson(person);

            List<ShoppingCartDetails> items = new ArrayList<>();
            ShoppingCart existingCart = new ShoppingCart(1, person, ShoppingCartStatusEnum.ACTIVE, null, null, items);
            Product product = new Product();
            product.setId(1);
            NewCartRequest request = new NewCartRequest(
                    List.of(new CartProductRequest(BigDecimal.TEN, 1, 1)));
            ShoppingCartResponse response = new ShoppingCartResponse();

            when(userRepository.findByUsernameAndIsActiveTrue("client1")).thenReturn(Optional.of(user));
            when(shoppingCartRepository.findByClientIdAndStatus(10, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(existingCart));
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(1))
                    .thenReturn(Optional.of(product));
            when(cartMapper.toResponse(existingCart)).thenReturn(response);

            ShoppingCartResponse result = cartService.createCart(request);

            assertThat(result).isNotNull();
            verify(shoppingCartRepository).save(existingCart);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when a product in the request is not found")
        void throwsWhenProductNotFound() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("client1", null));
            Person person = new Person();
            person.setId(5);
            SysUser user = new SysUser();
            user.setId(10);
            user.setPerson(person);

            NewCartRequest request = new NewCartRequest(
                    List.of(new CartProductRequest(BigDecimal.TEN, 1, 99)));

            when(userRepository.findByUsernameAndIsActiveTrue("client1")).thenReturn(Optional.of(user));
            when(shoppingCartRepository.findByClientIdAndStatus(10, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(99))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.createCart(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            verify(shoppingCartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("addItemToCart")
    class AddItemToCart {

        @Test
        @DisplayName("adds new item to cart when product is not already present")
        void addsNewItemToCart() {
            List<ShoppingCartDetails> items = new ArrayList<>();
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);
            ProductStock stock = new ProductStock();
            stock.setQuantity(10);
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            product.setStock(List.of(stock));
            ShoppingCartResponse response = new ShoppingCartResponse();

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(1))
                    .thenReturn(Optional.of(product));
            when(cartMapper.toResponse(cart)).thenReturn(response);

            ShoppingCartResponse result = cartService.addItemToCart(1, new CartProductRequest(BigDecimal.TEN, 2, 1));

            assertThat(result).isNotNull();
            assertThat(items).hasSize(1);
            verify(shoppingCartRepository).save(cart);
        }

        @Test
        @DisplayName("updates quantity and price when item is already in the cart")
        void updatesQuantityForExistingItem() {
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            ProductStock stock = new ProductStock();
            stock.setQuantity(20);
            product.setStock(List.of(stock));

            ShoppingCartDetails existingItem = ShoppingCartDetails.builder()
                    .cart(null)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(3)
                    .build();
            List<ShoppingCartDetails> items = new ArrayList<>();
            items.add(existingItem);
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);
            ShoppingCartResponse response = new ShoppingCartResponse();

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(1))
                    .thenReturn(Optional.of(product));
            when(cartMapper.toResponse(cart)).thenReturn(response);

            cartService.addItemToCart(1, new CartProductRequest(new BigDecimal("12.00"), 5, 1));

            assertThat(existingItem.getQuantity()).isEqualTo(8); // 3 + 5
            assertThat(existingItem.getPrice()).isEqualByComparingTo("12.00");
            verify(shoppingCartRepository).save(cart);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart not found")
        void throwsWhenCartNotFound() {
            when(shoppingCartRepository.findByIdAndStatus(99, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItemToCart(99,
                    new CartProductRequest(BigDecimal.TEN, 1, 1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cart not found");
            verify(shoppingCartRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void throwsWhenProductNotFound() {
            List<ShoppingCartDetails> items = new ArrayList<>();
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(99))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItemToCart(1,
                    new CartProductRequest(BigDecimal.TEN, 1, 99)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            verify(shoppingCartRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BadRequestException when requested quantity exceeds available stock")
        void throwsWhenInsufficientStock() {
            ProductStock stock = new ProductStock();
            stock.setQuantity(1);
            Product product = new Product();
            product.setId(1);
            product.setStock(List.of(stock));
            List<ShoppingCartDetails> items = new ArrayList<>();
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(productRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(1))
                    .thenReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItemToCart(1,
                    new CartProductRequest(BigDecimal.TEN, 5, 1)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");
            verify(shoppingCartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeItemFromCart")
    class RemoveItemFromCart {

        @Test
        @DisplayName("removes item from cart by item id")
        void removesItemFromCart() {
            Product product = new Product();
            product.setId(1);
            ShoppingCartDetails item = ShoppingCartDetails.builder()
                    .cart(null)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(1)
                    .build();
            item.setId(5);
            List<ShoppingCartDetails> items = new ArrayList<>();
            items.add(item);
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);
            ShoppingCartResponse response = new ShoppingCartResponse();

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(cartMapper.toResponse(cart)).thenReturn(response);

            ShoppingCartResponse result = cartService.removeItemFromCart(1, 5);

            assertThat(result).isNotNull();
            assertThat(items).isEmpty();
            verify(shoppingCartRepository).save(cart);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart not found")
        void throwsWhenCartNotFound() {
            when(shoppingCartRepository.findByIdAndStatus(99, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeItemFromCart(99, 5))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cart not found");
            verify(shoppingCartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCartByClientId")
    class GetCartByClientId {

        private SysUser setupUser(String username, RoleEnum roleEnum, int personId) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null));
            Person person = new Person();
            person.setId(personId);
            Role role = new Role();
            role.setName(roleEnum);
            SysUser user = new SysUser();
            user.setId(100);
            user.setUsername(username);
            user.setPerson(person);
            user.setRole(role);
            when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.of(user));
            return user;
        }

        @Test
        @DisplayName("returns existing cart when client has active cart")
        void returnsExistingCartForClient() {
            setupUser("client1", RoleEnum.CLIENT, 5);

            Person client = new Person();
            client.setId(5);
            List<ShoppingCartDetails> items = new ArrayList<>();
            ShoppingCart cart = new ShoppingCart(1, client, ShoppingCartStatusEnum.ACTIVE, null, null, items);

            when(shoppingCartRepository.findByClientIdAndStatus(5, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));

            ShoppingCartResponse response = cartService.getCartByClientId(5);

            assertThat(response.getId()).isEqualTo(1);
            assertThat(response.getClient_id()).isEqualTo(5);
            assertThat(response.getStatus()).isEqualTo(ShoppingCartStatusEnum.ACTIVE.toString());
            verify(shoppingCartRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates new empty cart when client has no active cart")
        void createsNewCartWhenNoneExists() {
            setupUser("client1", RoleEnum.CLIENT, 5);

            Person person = new Person();
            person.setId(5);
            when(shoppingCartRepository.findByClientIdAndStatus(5, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());
            when(personRepository.findById(5)).thenReturn(Optional.of(person));

            ShoppingCartResponse response = cartService.getCartByClientId(5);

            assertThat(response.getClient_id()).isEqualTo(5);
            assertThat(response.getProducts()).isEmpty();
            verify(shoppingCartRepository).save(any(ShoppingCart.class));
        }

        @Test
        @DisplayName("throws AccessDeniedException when non-manager tries to access other client cart")
        void throwsWhenClientAccessesOtherClientCart() {
            setupUser("client1", RoleEnum.CLIENT, 5);

            assertThatThrownBy(() -> cartService.getCartByClientId(99))
                    .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                    .hasMessageContaining("You do not own this cart");

            verify(shoppingCartRepository, never()).findByClientIdAndStatus(anyInt(), any());
        }

        @Test
        @DisplayName("allows manager to access any client cart")
        void managerCanAccessAnyClientCart() {
            setupUser("manager1", RoleEnum.MANAGER, 0);

            Person client = new Person();
            client.setId(7);
            ShoppingCart cart = new ShoppingCart(2, client, ShoppingCartStatusEnum.ACTIVE, null, null, List.of());

            when(shoppingCartRepository.findByClientIdAndStatus(7, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));

            ShoppingCartResponse response = cartService.getCartByClientId(7);

            assertThat(response.getId()).isEqualTo(2);
            assertThat(response.getClient_id()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("updateCartItem")
    class UpdateCartItem {

        @Test
        @DisplayName("updates price and quantity when stock is sufficient")
        void updatesItemWhenStockSufficient() {
            Product product = new Product();
            product.setId(1);
            ProductStock stock = new ProductStock();
            stock.setQuantity(10);
            product.setStock(List.of(stock));

            ShoppingCartDetails item = ShoppingCartDetails.builder()
                    .cart(null)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(2)
                    .build();
            item.setId(5);

            List<ShoppingCartDetails> items = new ArrayList<>();
            items.add(item);
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);
            ShoppingCartResponse response = new ShoppingCartResponse();

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(cartMapper.toResponse(cart)).thenReturn(response);

            CartProductRequest req = new CartProductRequest(new BigDecimal("15.00"), 3, 1);

            ShoppingCartResponse result = cartService.updateCartItem(1, 5, req);

            assertThat(result).isSameAs(response);
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getPrice()).isEqualByComparingTo("15.00");
            verify(shoppingCartRepository).save(cart);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart not found")
        void throwsWhenCartNotFound() {
            when(shoppingCartRepository.findByIdAndStatus(99, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateCartItem(99, 5,
                    new CartProductRequest(BigDecimal.TEN, 1, 1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Active cart not found");
            verify(shoppingCartRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart item not found")
        void throwsWhenItemNotFound() {
            List<ShoppingCartDetails> items = new ArrayList<>();
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> cartService.updateCartItem(1, 5,
                    new CartProductRequest(BigDecimal.TEN, 1, 1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cart item not found");
            verify(shoppingCartRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BadRequestException when new quantity exceeds available stock")
        void throwsWhenInsufficientStock() {
            Product product = new Product();
            product.setId(1);
            ProductStock stock = new ProductStock();
            stock.setQuantity(2);
            product.setStock(List.of(stock));

            ShoppingCartDetails item = ShoppingCartDetails.builder()
                    .cart(null)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(2)
                    .build();
            item.setId(5);

            List<ShoppingCartDetails> items = new ArrayList<>();
            items.add(item);
            ShoppingCart cart = new ShoppingCart(1, null, ShoppingCartStatusEnum.ACTIVE, null, null, items);

            when(shoppingCartRepository.findByIdAndStatus(1, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));

            CartProductRequest req = new CartProductRequest(BigDecimal.TEN, 5, 1);

            assertThatThrownBy(() -> cartService.updateCartItem(1, 5, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(shoppingCartRepository, never()).save(any());
        }
    }
}
