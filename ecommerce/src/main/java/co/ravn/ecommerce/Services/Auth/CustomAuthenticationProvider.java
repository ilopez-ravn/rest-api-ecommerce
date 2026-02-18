package co.ravn.ecommerce.Services.Auth;

import co.ravn.ecommerce.Utils.Constants;
import lombok.extern.slf4j.Slf4j;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
