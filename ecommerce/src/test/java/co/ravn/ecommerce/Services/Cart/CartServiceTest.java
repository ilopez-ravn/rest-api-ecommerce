package co.ravn.ecommerce.Services.Cart;

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
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
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

            ShoppingCartDetails existingItem = new ShoppingCartDetails(null, product, BigDecimal.TEN, 3);
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
            ShoppingCartDetails item = new ShoppingCartDetails(null, product, BigDecimal.TEN, 1);
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
}
