package co.ravn.ecommerce.services.auth;

import co.ravn.ecommerce.dto.request.auth.UserRegisterRequest;
import co.ravn.ecommerce.dto.response.auth.UserDetailsResponse;
import co.ravn.ecommerce.entities.auth.Role;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.exception.ConflictException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.auth.AuthMapper;
import co.ravn.ecommerce.repositories.auth.PasswordRecoveryTokenRepository;
import co.ravn.ecommerce.repositories.auth.PersonRepository;
import co.ravn.ecommerce.repositories.auth.RoleRepository;
import co.ravn.ecommerce.repositories.auth.UserRefreshTokenRepository;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.utils.enums.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            SysUser user = new SysUser();
            user.setId(1);
            user.setUsername("admin");
            when(userRepository.findByUsernameAndIsActiveTrue("admin")).thenReturn(Optional.of(user));

            SysUser result = authService.loadUserByUsername("admin");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("admin");
            verify(userRepository).findByUsernameAndIsActiveTrue("admin");
        }

        @Test
        @DisplayName("throws UsernameNotFoundException when not found")
        void throwsWhenNotFound() {
            when(userRepository.findByUsernameAndIsActiveTrue("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.loadUserByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("saves and returns user when role exists and username unique")
        void createsWhenRoleExistsAndUsernameUnique() {
            UserRegisterRequest request = new UserRegisterRequest("newuser", "pass123", 1);
            Role role = new Role();
            role.setId(1);
            role.setName(RoleEnum.CLIENT);
            SysUser savedUser = new SysUser("newuser", "encoded", role);
            savedUser.setId(1);
            UserDetailsResponse response = new UserDetailsResponse();
            response.setId(1);
            response.setUsername("newuser");

            when(roleRepository.findById(1)).thenReturn(Optional.of(role));
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(encoder.encode("pass123")).thenReturn("encoded");
            when(userRepository.save(any(SysUser.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authMapper.toResponse(any(SysUser.class))).thenReturn(response);

            UserDetailsResponse result = authService.createUser(request);

            assertThat(result.getUsername()).isEqualTo("newuser");
            verify(userRepository).save(any(SysUser.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when role not found")
        void throwsWhenRoleNotFound() {
            UserRegisterRequest request = new UserRegisterRequest("u", "p", 99);
            when(roleRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.createUser(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Role");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when username already exists")
        void throwsWhenUsernameExists() {
            UserRegisterRequest request = new UserRegisterRequest("existing", "p", 1);
            Role role = new Role();
            role.setId(1);
            when(roleRepository.findById(1)).thenReturn(Optional.of(role));
            when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new SysUser()));

            assertThatThrownBy(() -> authService.createUser(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");
            verify(userRepository, never()).save(any());
        }
    }
}
