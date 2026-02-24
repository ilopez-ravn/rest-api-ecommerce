package co.ravn.ecommerce.Services.Auth;

import co.ravn.ecommerce.DTO.PasswordResetEvent;
import co.ravn.ecommerce.DTO.Request.Auth.*;
import co.ravn.ecommerce.DTO.Response.Auth.LoginResponse;
import co.ravn.ecommerce.DTO.Response.Auth.RefreshTokenResponse;
import co.ravn.ecommerce.DTO.Response.Auth.UserDetailsResponse;
import co.ravn.ecommerce.DTO.Response.MessageResponse;
import co.ravn.ecommerce.Entities.Auth.PasswordRecoveryToken;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Auth.Role;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Auth.UserRefreshToken;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Auth.AuthMapper;
import co.ravn.ecommerce.Repositories.Auth.PasswordRecoveryTokenRepository;
import co.ravn.ecommerce.Repositories.Auth.PersonRepository;
import co.ravn.ecommerce.Repositories.Auth.RoleRepository;
import co.ravn.ecommerce.Repositories.Auth.UserRefreshTokenRepository;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Utils.Constants;
import co.ravn.ecommerce.Utils.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public UserDetailsResponse createUser(UserRegisterRequest userRegisterRequest) {
        Role role = roleRepository.findById(userRegisterRequest.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with ID " + userRegisterRequest.getRoleId() + " not found"));

        if (userRepository.findByUsername(userRegisterRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        SysUser sysUser = new SysUser(
                userRegisterRequest.getUsername(),
                encoder.encode(userRegisterRequest.getPassword()),
                role);

        log.info("Creating user with username={}", userRegisterRequest.getUsername());
        userRepository.save(sysUser);
        return authMapper.toResponse(sysUser);
    }

    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.info("authenticateUser: Authenticating user with username={}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        if (!authentication.isAuthenticated()) {
            log.error(Constants.BAD_CREDENTIALS_MESSAGE);
            throw new BadCredentialsException(Constants.BAD_CREDENTIALS_MESSAGE);
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
        return authMapper.toLoginResponse(sysUser, accessToken, refreshToken.getRefreshToken());
    }

    public UserDetailsResponse getUserById(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = this.loadUserByUsername(auth.getName());

        SysUser sysUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));

        if (!currentUser.getRole().getName().equals(RoleEnum.MANAGER) && currentUser.getId() != id) {
            throw new AccessDeniedException("Access denied");
        }

        return authMapper.toResponse(sysUser);
    }

    public RefreshTokenResponse generateAccessToken(RefreshTokenRequest refreshTokenRequest) {
        if (jwtService.isTokenExpired(refreshTokenRequest.getRefreshToken())) {
            throw new BadCredentialsException("Refresh token has expired");
        }

        userRefreshTokenRepository.findByRefreshToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        String username = jwtService.extractUsername(refreshTokenRequest.getRefreshToken());
        SysUser sysUser = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + " not found"));

        String accessToken = jwtService.generateAccessToken(sysUser);
        return new RefreshTokenResponse(accessToken);
    }

    @Transactional
    public MessageResponse logoutUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = this.loadUserByUsername(auth.getName());

        userRefreshTokenRepository.deleteByUserId(currentUser.getId());

        // TODO: Create a blacklist of tokens in database to invalidate access tokens until they expire

        return new MessageResponse("User logged out successfully");
    }

    @Transactional
    public MessageResponse requestPasswordResetToken(PasswordResetEmailRequest request) {
        log.info("Searching email={}", request.getEmail());
        Person personData = personRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + request.getEmail() + " not found"));

        String token = jwtService.generatePasswordResetToken(personData.getSysUser());
        log.info("Reset password token generated for email={}", request.getEmail());

        passwordRecoveryTokenRepository.deleteByUserId(personData.getSysUser().getId());

        publisher.publishEvent(new PasswordResetEvent(
                personData.getSysUser(),
                request.getEmail(),
                personData.getFullName(),
                token,
                jwtService.extractExpiration(token)));

        return new MessageResponse("Password reset link sent to email");
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest passwordResetRequest) {
        if (jwtService.isTokenExpired(passwordResetRequest.getToken())) {
            throw new BadCredentialsException("Password reset token has expired");
        }

        String username = jwtService.extractUsername(passwordResetRequest.getToken());
        SysUser sysUser = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with username " + username + " not found"));

        PasswordRecoveryToken token = passwordRecoveryTokenRepository
                .findByRecoveryToken(passwordResetRequest.getToken())
                .orElseThrow(() -> new RuntimeException(
                        "Password recovery token not found: " + passwordResetRequest.getToken()));

        sysUser.setPassword(encoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(sysUser);
        passwordRecoveryTokenRepository.delete(token);

        return new MessageResponse("Password has been reset successfully");
    }

    public List<UserDetailsResponse> getUserList() {
        return userRepository.findByIsActiveTrueWithPerson().stream()
                .map(authMapper::toResponse)
                .toList();
    }
}
