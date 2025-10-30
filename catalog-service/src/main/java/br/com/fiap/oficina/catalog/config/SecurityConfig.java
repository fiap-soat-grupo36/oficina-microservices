package br.com.fiap.oficina.catalog.config;

import br.com.fiap.oficina.shared.security.JwtAuthenticationFilter;
import br.com.fiap.oficina.shared.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String ATENDENTE = "ATENDENTE";
    private static final String MECANICO = "MECANICO";
    
    private static final String[] PUBLIC_GET = {
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/h2-console/**"
    };

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(JwtTokenProvider tokenProvider,
                         UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                        // Produtos do Catálogo - /api/catalogo-produtos
                        .requestMatchers(HttpMethod.POST, "/api/catalogo-produtos/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/catalogo-produtos/**").hasAnyRole(ADMIN, ATENDENTE, MECANICO)
                        .requestMatchers(HttpMethod.PUT, "/api/catalogo-produtos/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/catalogo-produtos/**").hasRole(ADMIN)
                        // Serviços - /api/servicos
                        .requestMatchers(HttpMethod.POST, "/api/servicos/**").hasAnyRole(ADMIN, ATENDENTE)
                        .requestMatchers(HttpMethod.GET, "/api/servicos/**").hasAnyRole(ADMIN, ATENDENTE, MECANICO)
                        .requestMatchers(HttpMethod.PUT, "/api/servicos/**").hasAnyRole(ADMIN, ATENDENTE)
                        .requestMatchers(HttpMethod.DELETE, "/api/servicos/**").hasAnyRole(ADMIN, ATENDENTE)
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
