package com.jpos.bluejay.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/users", "/api/users/**").hasAuthority("ROLE_Admin")
                        .requestMatchers("/api/inventory", "/api/inventory/**").hasAnyAuthority("ROLE_Admin", "ROLE_Manager")
                        .requestMatchers("/api/sales/prices", "/api/sales/prices/**").hasAnyAuthority("ROLE_Admin", "ROLE_Manager", "ROLE_Cashier")
                        .requestMatchers("/api/sales/products", "/api/sales/products/**").hasAnyAuthority("ROLE_Admin", "ROLE_Manager", "ROLE_Cashier")
                        .requestMatchers("/api/sales/transactions", "/api/sales/transactions/**").hasAnyAuthority("ROLE_Admin", "ROLE_Manager", "ROLE_Cashier")
                        .requestMatchers("/api/reports", "/api/reports/**").hasAnyAuthority("ROLE_Admin", "ROLE_Manager")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
