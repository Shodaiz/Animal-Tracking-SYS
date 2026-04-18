package com.example.backend.security;

import com.example.backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (Login)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Fichiers statiques (HTML, CSS, JS)
                        .requestMatchers("/", "/index.html", "/test.html",
                                "/css/**", "/js/**", "/images/**").permitAll()

                        // Routes Fermier uniquement
                        .requestMatchers("/api/farmer/**")
                        .hasAuthority("ROLE_FARMER")

                        // Routes Vétérinaire uniquement
                        .requestMatchers("/api/vet/**")
                        .hasAuthority("ROLE_VET")

                        // Routes Contrôleur uniquement
                        .requestMatchers("/api/controller/**")
                        .hasAuthority("ROLE_CONTROLLER")

                        // Routes Constat - Contrôleur uniquement
                        .requestMatchers("/api/constat/**")
                        .hasAuthority("ROLE_CONTROLLER")

                        // Toutes les autres routes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}