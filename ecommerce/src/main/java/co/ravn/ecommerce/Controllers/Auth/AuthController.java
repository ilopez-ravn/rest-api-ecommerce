package co.ravn.ecommerce.Controllers.Auth;

import co.ravn.ecommerce.DTO.Request.Auth.*;
import co.ravn.ecommerce.Services.Auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/users")
public class AuthController {
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<?> getUsers() {
        return authService.getUserList();
    }

    @PostMapping("")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        return authService.createUser(userRegisterRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PutMapping("/signout")
    public ResponseEntity<?> logout() {
        return authService.logoutUser();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return authService.generateAccessToken(refreshTokenRequest);
    }

    // User handling

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable @Min(1) int id) {
        return authService.getUserById(id);
    }


    // Password recovery

    @PostMapping("/password/token")
    public ResponseEntity<?> requestPasswordResetToken(@RequestBody @Valid PasswordResetEmailRequest request) {
        return authService.requestPasswordResetToken(request);
    }

    @PutMapping("/password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        return authService.resetPassword(passwordResetRequest);
    }

}
