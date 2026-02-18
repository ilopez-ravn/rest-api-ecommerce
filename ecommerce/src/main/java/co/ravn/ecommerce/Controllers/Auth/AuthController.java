package co.ravn.ecommerce.Controllers.Auth;

import co.ravn.ecommerce.DTO.Request.Auth.*;
import co.ravn.ecommerce.Services.Auth.AuthService;
import jakarta.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/users")
public class AuthController {
    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<?> getUsers() {
        return authService.getUserList();
    }

    @PostMapping("")
    public ResponseEntity<?> createUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        return authService.createUser(userRegisterRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PutMapping("/signout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
        return authService.logoutUser(logoutRequest);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.generateAccessToken(refreshTokenRequest);
    }

    // User handling

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable @Min(1) int id) {

        return authService.getUserById(id);
    }


    // Password recovery

    @PostMapping("/password/token")
    public ResponseEntity<?> requestPasswordResetToken(@RequestBody PasswordResetEmailRequest request) {
        return authService.requestPasswordResetToken(request);
    }

    @PutMapping("/password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
        return authService.resetPassword(passwordResetRequest);
    }

}
