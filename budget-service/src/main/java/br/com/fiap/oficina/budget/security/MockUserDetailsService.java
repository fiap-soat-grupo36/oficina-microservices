package br.com.fiap.oficina.budget.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of UserDetailsService for development/testing purposes.
 * In production, this should be replaced with a proper integration with auth-service
 * or use a Feign client to fetch user details from the authentication service.
 */
@Service
public class MockUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Mock implementation - empty password is acceptable here as this is only for 
        // JWT token validation, not actual password authentication
        // In production, integrate with auth-service for real user details
        return User.builder()
                .username(username)
                .password("") // Not used for JWT validation
                .roles("ADMIN")
                .build();
    }
}
