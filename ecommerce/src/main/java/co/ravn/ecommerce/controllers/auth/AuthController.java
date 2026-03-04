package co.ravn.ecommerce.controllers.auth;

import co.ravn.ecommerce.dto.request.auth.*;
import co.ravn.ecommerce.dto.response.auth.LoginResponse;
import co.ravn.ecommerce.dto.response.auth.RefreshTokenResponse;
import co.ravn.ecommerce.dto.response.auth.UserDetailsResponse;
import co.ravn.ecommerce.dto.response.MessageResponse;
import co.ravn.ecommerce.services.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/users")
public class AuthController {

    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<UserDetailsResponse>> getUsers() {
        return ResponseEntity.ok(authService.getUserList());
    }

    @PostMapping("")
    public ResponseEntity<UserDetailsResponse> createUser(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(authService.createUser(userRegisterRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PutMapping("/signout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(authService.logoutUser());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.generateAccessToken(refreshTokenRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsResponse> getUserById(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PostMapping("/password/token")
    public ResponseEntity<MessageResponse> requestPasswordResetToken(@RequestBody @Valid PasswordResetEmailRequest request) {
        return ResponseEntity.ok(authService.requestPasswordResetToken(request));
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        return ResponseEntity.ok(authService.resetPassword(passwordResetRequest));
    }
}
