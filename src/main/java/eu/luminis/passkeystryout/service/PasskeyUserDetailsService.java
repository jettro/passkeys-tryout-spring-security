package eu.luminis.passkeystryout.service;

import eu.luminis.passkeystryout.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PasskeyUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public PasskeyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        eu.luminis.passkeystryout.entity.User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities("ROLE_USER")
            .build();
    }
}
