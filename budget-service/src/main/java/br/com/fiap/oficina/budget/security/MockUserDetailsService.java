package br.com.fiap.oficina.budget.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MockUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Mock implementation - em produção, deve buscar usuário do auth-service
        return User.builder()
                .username(username)
                .password("")
                .roles("ADMIN")
                .build();
    }
}
