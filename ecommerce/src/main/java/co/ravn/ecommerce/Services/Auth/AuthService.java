package co.ravn.ecommerce.Services.Auth;

import co.ravn.ecommerce.DTO.PasswordResetEvent;
import co.ravn.ecommerce.DTO.Request.Auth.*;
import co.ravn.ecommerce.DTO.Response.Auth.LoginResponse;
import co.ravn.ecommerce.DTO.Response.Auth.RefreshTokenResponse;
import co.ravn.ecommerce.DTO.Response.Auth.UserDetailsResponse;
import co.ravn.ecommerce.Mappers.Auth.AuthMapper;
import co.ravn.ecommerce.Entities.Auth.PasswordRecoveryToken;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Auth.Role;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Auth.PasswordRecoveryTokenRepository;
import co.ravn.ecommerce.Repositories.Auth.PersonRepository;
import co.ravn.ecommerce.Repositories.Auth.RoleRepository;
import co.ravn.ecommerce.Utils.Constants;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Auth.UserRefreshToken;
import co.ravn.ecommerce.DTO.Response.ErrorResponse;
import co.ravn.ecommerce.DTO.Response.MessageResponse;
import co.ravn.ecommerce.Repositories.Auth.UserRefreshTokenRepository;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {

    private UserRefreshTokenRepository userRefreshTokenRepository;

    private JWTService jwtService;

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private PersonRepository personRepository;

    private RoleRepository roleRepository;

    private ApplicationEventPublisher publisher;

    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    private final PasswordEncoder encoder;

    private final AuthMapper authMapper;

    public SysUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(Constants.BAD_CREDENTIALS_MESSAGE));
    }

    public SysUser loadUserByUsernameWithPerson(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameAndIsActiveTrueWithPerson(username)
                .orElseThrow(() -> new UsernameNotFoundException(Constants.BAD_CREDENTIALS_MESSAGE));
    }

    public ResponseEntity<?> createUser(UserRegisterRequest userRegisterRequest) {
        Optional<Role> role = roleRepository.findById(userRegisterRequest.getRoleId());
        if (role.isEmpty())
            throw new ResourceNotFoundException(
                    "Role with ID " + userRegisterRequest.getRoleId() + " not found");

        // Validate username
        Optional<SysUser> existingUser = userRepository.findByUsername(userRegisterRequest.getUsername());
        if (existingUser.isPresent())
            throw new RuntimeException("Username already exists");

        SysUser sysUser = new SysUser(
                userRegisterRequest.getUsername(),
                encoder.encode(userRegisterRequest.getPassword()),
                role.get());

        log.info("Creating user with username={}", userRegisterRequest.getUsername());

        userRepository.save(sysUser);

        return ResponseEntity.ok().body(authMapper.toResponse(sysUser));

    }

    @Transactional
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {

        log.info("authenticateUser: Authenticating user with username={}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            log.info("Authenticated:{}", authentication.isAuthenticated());

            if (!authentication.isAuthenticated()) {
                log.error(Constants.BAD_CREDENTIALS_MESSAGE);

                ErrorResponse errorResponse = new ErrorResponse(
                        Constants.BAD_CREDENTIALS_CODE,
                        Constants.BAD_CREDENTIALS_MESSAGE);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse);
            }

            log.info("authenticateUser: User found");

            SysUser sysUser = this.loadUserByUsernameWithPerson(loginRequest.getUsername());
            String accessToken = jwtService.generateAccessToken(sysUser);
            UserRefreshToken refreshToken = new UserRefreshToken(
                    sysUser,
                    jwtService.generateRefreshToken(sysUser),
                    jwtService.getRefreshTokenExpiry());

            userRefreshTokenRepository.save(refreshToken);

            log.info("authenticateUser: User authenticated");

            return ResponseEntity.ok()
                    .body(authMapper.toLoginResponse(sysUser, accessToken, refreshToken.getRefreshToken()));

        } catch (BadCredentialsException e) {
            log.error("authenticateUser: Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            Constants.BAD_CREDENTIALS_CODE,
                            Constants.BAD_CREDENTIALS_MESSAGE));
        }
    }

    public ResponseEntity<?> getUserById(@Min(1) int id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = this.loadUserByUsername(auth.getName());

        Optional<SysUser> sysUser = userRepository.findById(id);

        if (sysUser.isEmpty())
            throw new ResourceNotFoundException("User with ID " + id + " not found");

        // Protect user info from other users except managers
        if (!(currentUser.getRole().getName().toString().equals("MANAGER")) && currentUser.getId() != id)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            String.valueOf(HttpStatus.FORBIDDEN.value()),
                            HttpStatus.FORBIDDEN.getReasonPhrase()));

        return ResponseEntity.ok()
                .body(authMapper.toResponse(sysUser.get()));
    }

    public ResponseEntity<?> generateAccessToken(RefreshTokenRequest refreshTokenRequest) {
        if (jwtService.isTokenExpired(refreshTokenRequest.getRefreshToken()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase()));

        // Check if the refresh token exists in database
        Optional<UserRefreshToken> userRefreshToken = userRefreshTokenRepository
                .findByRefreshToken(refreshTokenRequest.getRefreshToken());
        if (userRefreshToken.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase()));

        String username = jwtService.extractUsername(refreshTokenRequest.getRefreshToken());
        Optional<SysUser> sysUser = userRepository.findByUsernameAndIsActiveTrue(username);
        if (sysUser.isEmpty())
            throw new ResourceNotFoundException("User with username " + username + " not found");

        String accessToken = jwtService.generateAccessToken(sysUser.get());

        return ResponseEntity.ok()
                .body(new RefreshTokenResponse(
                        accessToken));
    }

    @Transactional
    public ResponseEntity<?> logoutUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = this.loadUserByUsername(auth.getName());

        userRefreshTokenRepository.deleteByUserId(currentUser.getId());

        // TODO: Create a blacklist of tokens in database to invalidate access tokens
        // until they expire

        return ResponseEntity.ok()
                .body(new MessageResponse("User logged out successfully"));
    }

    @Transactional
    public ResponseEntity<?> requestPasswordResetToken(PasswordResetEmailRequest request) {
        log.info("Searching email=" + request.getEmail());
        Person personData = personRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + request.getEmail() + " not found"));

        String token = jwtService.generatePasswordResetToken(personData.getSysUser());
        log.info("Reset password token generated for email=" + request.getEmail());

        // delete current password reset tokens
        passwordRecoveryTokenRepository.deleteByUserId(personData.getSysUser().getId());

        publisher.publishEvent(new PasswordResetEvent(
                personData.getSysUser(),
                request.getEmail(),
                personData.getFullName(),
                token,
                jwtService.extractExpiration(token)));

        return ResponseEntity.ok().body(new MessageResponse("Password reset link sent to email"));
    }

    @Transactional
    public ResponseEntity<?> resetPassword(PasswordResetRequest passwordResetRequest) {
        // Validate the token and extract the username
        if (jwtService.isTokenExpired(passwordResetRequest.getToken()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase()));

        String username = jwtService.extractUsername(passwordResetRequest.getToken());
        SysUser sysUser = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with username " + username + " not found"));

        // Search for the token in database for one use only and delete it after sending
        // the email
        PasswordRecoveryToken token = passwordRecoveryTokenRepository
                .findByRecoveryToken(passwordResetRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Password recovery token not found: "
                        + passwordResetRequest.getToken()));
        // Update the user's password
        sysUser.setPassword(encoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(sysUser);
        passwordRecoveryTokenRepository.delete(token);

        return ResponseEntity.ok().body(new MessageResponse("Password has been reset successfully"));
    }

    public ResponseEntity<?> getUserList() {
        List<SysUser> sysUsers = userRepository.findByIsActiveTrueWithPerson();

        List<UserDetailsResponse> userDetails = sysUsers.stream()
                .map(authMapper::toResponse)
                .toList();

        return ResponseEntity.ok()
                .body(userDetails);
    }
}
