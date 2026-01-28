package co.ravn.ecommerce.Services;

import co.ravn.ecommerce.Utils.Constants;
import co.ravn.ecommerce.Entities.SysUser;
import co.ravn.ecommerce.Entities.UserRefreshToken;
import co.ravn.ecommerce.Models.ErrorResponse;
import co.ravn.ecommerce.Models.Users.*;
import co.ravn.ecommerce.Repositories.UserRefreshTokenRepository;
import co.ravn.ecommerce.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder encoder;

    @Autowired
    public AuthService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }


    public SysUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(Constants.BAD_CREDENTIALS_MESSAGE));
    }


    public ResponseEntity<?> createUser(UserRegisterRequest userRegisterRequest) {
        SysUser sysUser = new SysUser(
                userRegisterRequest.getUsername(),
                encoder.encode(userRegisterRequest.getPassword()),
                userRegisterRequest.getRoleId()
        );

        logger.info("Creating user with username={} and password={}", userRegisterRequest.getUsername(), userRegisterRequest.getPassword());

        userRepository.save(sysUser);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", Constants.SUCCESS_USER_CREATED_MESSAGE);

        return ResponseEntity.ok(responseBody);

    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {

        logger.info("authenticateUser: Authenticating user with username={}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            logger.info("Authenticated:{}", authentication.isAuthenticated());

            if (!authentication.isAuthenticated()) {
                logger.error(Constants.BAD_CREDENTIALS_MESSAGE);

                ErrorResponse errorResponse = new ErrorResponse(
                        Constants.BAD_CREDENTIALS_CODE,
                        Constants.BAD_CREDENTIALS_MESSAGE
                );

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse);
            }

            logger.info("authenticateUser: User found");

            SysUser sysUser = this.loadUserByUsername(loginRequest.getUsername());
            String accessToken = jwtService.generateAccessToken(sysUser);
            UserRefreshToken refreshToken = new UserRefreshToken(
                    sysUser,
                    jwtService.generateRefreshToken(sysUser),
                    jwtService.getRefreshTokenExpiry()
            );

            userRefreshTokenRepository.save(refreshToken);

            logger.info("authenticateUser: User authenticated");

            return ResponseEntity.ok()
                    .body(new LoginResponse(
                            accessToken,
                            refreshToken.getRefreshToken()
                    ));

        } catch (BadCredentialsException e) {
            logger.error("authenticateUser: Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            Constants.BAD_CREDENTIALS_CODE,
                            Constants.BAD_CREDENTIALS_MESSAGE
                    ));
        } catch (Exception e) {
            logger.error("authenticateUser: unexpected error");
            logger.error(String.valueOf(e));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            Constants.INTERNAL_SERVER_ERROR_CODE,
                            Constants.INTERNAL_SERVER_ERROR_MESSAGE
                    ));
        }
    }
}
