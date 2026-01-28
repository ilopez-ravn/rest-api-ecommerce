package co.ravn.ecommerce.Controllers;

import co.ravn.ecommerce.Models.Users.UserRegisterRequest;
import co.ravn.ecommerce.Models.Users.LoginRequest;
import co.ravn.ecommerce.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/")
    private ResponseEntity<?> createUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        return authService.createUser(userRegisterRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

}
