package co.ravn.ecommerce.services.auth;

import co.ravn.ecommerce.utils.Constants;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(Constants.BAD_CREDENTIALS_MESSAGE));


        return new CustomSecurityUser(sysUser);


    }

}
