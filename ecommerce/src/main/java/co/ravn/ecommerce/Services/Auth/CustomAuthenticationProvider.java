package co.ravn.ecommerce.Services.Auth;

import co.ravn.ecommerce.Utils.Constants;
import co.ravn.ecommerce.Entities.SysUser;
import co.ravn.ecommerce.Repositories.UserRepository;
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
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException(Constants.BAD_CREDENTIALS_MESSAGE));


        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println(Constants.BAD_CREDENTIALS_MESSAGE);
            throw new BadCredentialsException(Constants.BAD_CREDENTIALS_MESSAGE);
        }


        List<SimpleGrantedAuthority> authorityList = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRoleId())
        );

        return new UsernamePasswordAuthenticationToken(username, null, authorityList);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
