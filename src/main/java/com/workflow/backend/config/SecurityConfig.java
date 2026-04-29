package com.workflow.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/seed/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        // Departamentos: GET for ADMIN or FUNCIONARIO, mutations for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/departamentos").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/api/departamentos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/departamentos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/departamentos/**").hasRole("ADMIN")
                        // Analytics: ADMIN only
                        .requestMatchers("/api/analytics/**").hasRole("ADMIN")
                        // PDF: any authenticated user (ownership enforced in controller)
                        .requestMatchers("/api/pdf/**").authenticated()
                        // Iniciar tramite: ADMIN only
                        .requestMatchers(HttpMethod.PUT, "/api/tramites/{id}/iniciar").hasRole("ADMIN")
                        // Tareas por departamento: ADMIN or FUNCIONARIO
                        .requestMatchers("/api/tareas/departamento/**").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}