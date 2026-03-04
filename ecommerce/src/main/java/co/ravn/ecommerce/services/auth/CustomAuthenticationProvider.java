package co.ravn.ecommerce.services.auth;

import co.ravn.ecommerce.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        SysUser user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new BadCredentialsException(Constants.BAD_CREDENTIALS_MESSAGE));


        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.info(Constants.BAD_CREDENTIALS_MESSAGE);
            throw new BadCredentialsException(Constants.BAD_CREDENTIALS_MESSAGE);
        }


        List<SimpleGrantedAuthority> authorityList = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );

        return new UsernamePasswordAuthenticationToken(username, null, authorityList);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
