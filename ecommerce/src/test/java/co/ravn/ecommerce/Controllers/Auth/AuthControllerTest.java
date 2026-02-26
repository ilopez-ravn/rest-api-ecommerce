package co.ravn.ecommerce.Controllers.Auth;

import co.ravn.ecommerce.DTO.Request.Auth.LoginRequest;
import co.ravn.ecommerce.DTO.Request.Auth.RefreshTokenRequest;
import co.ravn.ecommerce.DTO.Request.Auth.UserRegisterRequest;
import co.ravn.ecommerce.DTO.Response.Auth.LoginResponse;
import co.ravn.ecommerce.DTO.Response.Auth.RefreshTokenResponse;
import co.ravn.ecommerce.DTO.Response.Auth.UserDetailsResponse;
import co.ravn.ecommerce.Exception.GlobalExceptionHandler;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Filters.JwtAuthFilter;
import co.ravn.ecommerce.Filters.RateLimitFilter;
import co.ravn.ecommerce.Services.Auth.AuthService;
import co.ravn.ecommerce.Utils.enums.RoleEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/users";

    @Nested
    @DisplayName("POST /api/v1/users/login")
    class Login {

        @Test
        @DisplayName("returns 200 and tokens when valid credentials")
        void login_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new LoginRequest("john", "secret"));
            LoginResponse response = new LoginResponse();
            response.setAccess_token("access-jwt");
            response.setRefresh_token("refresh-jwt");
            response.setId(1);
            response.setEmail("john@example.com");
            response.setFirstName("John");
            response.setLastName("Doe");
            response.setRole(RoleEnum.CLIENT);
            when(authService.authenticateUser(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").value("access-jwt"))
                    .andExpect(jsonPath("$.refresh_token").value("refresh-jwt"))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("john@example.com"));

            verify(authService).authenticateUser(any());
        }

        @Test
        @DisplayName("returns 401 when invalid credentials")
        void login_invalid_returns401() throws Exception {
            String body = objectMapper.writeValueAsString(new LoginRequest("john", "wrong"));
            when(authService.authenticateUser(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Bad credentials"));

            verify(authService).authenticateUser(any());
        }

        @Test
        @DisplayName("returns 400 when username is blank")
        void login_blankUsername_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new LoginRequest("", "secret"));

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(authService, never()).authenticateUser(any());
        }

        @Test
        @DisplayName("returns 400 when password is missing")
        void login_missingPassword_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new LoginRequest("john", null));

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).authenticateUser(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users (create user / register)")
    class CreateUser {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and user when valid body")
        void createUser_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new UserRegisterRequest("newuser", "pass123", 2));
            UserDetailsResponse response = new UserDetailsResponse();
            response.setId(1);
            response.setUsername("newuser");
            response.setEmail("new@example.com");
            response.setFirst_name("New");
            response.setLast_name("User");
            when(authService.createUser(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("newuser"));

            verify(authService).createUser(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when username is blank")
        void createUser_blankUsername_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new UserRegisterRequest("", "pass", 1));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(authService, never()).createUser(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when role not found")
        void createUser_roleNotFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(new UserRegisterRequest("u", "p", 999));
            when(authService.createUser(any()))
                    .thenThrow(new ResourceNotFoundException("Role not found"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(authService).createUser(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/refresh")
    class RefreshToken {

        @Test
        @DisplayName("returns 200 and new access token when valid refresh token")
        void refresh_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new RefreshTokenRequest("valid-refresh-token"));
            when(authService.generateAccessToken(any()))
                    .thenReturn(new RefreshTokenResponse("new-access-token"));

            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"));

            verify(authService).generateAccessToken(any());
        }

        @Test
        @DisplayName("returns 401 when refresh token expired or invalid")
        void refresh_invalid_returns401() throws Exception {
            String body = objectMapper.writeValueAsString(new RefreshTokenRequest("expired"));
            when(authService.generateAccessToken(any()))
                    .thenThrow(new BadCredentialsException("Refresh token has expired"));

            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());

            verify(authService).generateAccessToken(any());
        }

        @Test
        @DisplayName("returns 400 when refresh_token is missing")
        void refresh_missingToken_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new RefreshTokenRequest(null));

            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).generateAccessToken(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users and GET /api/v1/users/{id}")
    class GetUsers {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("GET users returns 200 and list")
        void getUsers_returns200() throws Exception {
            UserDetailsResponse user = new UserDetailsResponse();
            user.setId(1);
            user.setUsername("admin");
            when(authService.getUserList()).thenReturn(List.of(user));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].username").value("admin"));

            verify(authService).getUserList();
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("GET user by id returns 200 when found")
        void getUserById_returns200() throws Exception {
            UserDetailsResponse response = new UserDetailsResponse();
            response.setId(1);
            response.setUsername("john");
            response.setEmail("john@example.com");
            when(authService.getUserById(1)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("john"));

            verify(authService).getUserById(1);
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("GET user by id returns 404 when not found")
        void getUserById_notFound_returns404() throws Exception {
            when(authService.getUserById(999))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(authService).getUserById(999);
        }
    }
}
