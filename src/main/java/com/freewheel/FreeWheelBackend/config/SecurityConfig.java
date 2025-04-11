package com.freewheel.FreeWheelBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Puede ser necesaria
import org.springframework.security.config.http.SessionCreationPolicy; // Para APIs REST stateless
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF, común para APIs REST stateless
                .csrf(csrf -> csrf.disable())
                // Configura autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Permite todas las peticiones a /usuarios/** (ajusta según necesites)
                        .requestMatchers("/usuarios/**").permitAll()
                        // Permite todas las peticiones a /viajes/** (ajusta según necesites)
                        .requestMatchers("/viajes/**").permitAll()
                        // Cualquier otra petición requiere autenticación (si tuvieras login)
                        .anyRequest().authenticated() // O .permitAll() si todo es público por ahora
                )
                // Configura manejo de sesiones como stateless (común para APIs REST)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // Podrías añadir configuración de JWT, CORS, etc., aquí si lo necesitas

        return http.build();
    }
}