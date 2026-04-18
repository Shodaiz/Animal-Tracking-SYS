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

                // ✅ Routes publiques
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**").permitAll()

                // ✅ Rôles alignés sur l'ENUM de la BD finale (sans préfixe ROLE_)
                .requestMatchers("/api/farmer/**").hasAuthority("Farmer")
                .requestMatchers("/api/vet/**").hasAuthority("Veterinarian")
                .requestMatchers("/api/inspector/**").hasAuthority("Inspector")
                .requestMatchers("/api/admin/**").hasAuthority("Administrator")

                // Admin + Inspector peuvent accéder aux inspections
                .requestMatchers("/api/inspection/**")
                    .hasAnyAuthority("Inspector", "Administrator")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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