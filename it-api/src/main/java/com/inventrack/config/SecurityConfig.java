package com.inventrack.config;

import com.inventrack.repository.UserRepository;
import com.inventrack.security.JsonErrorWriter;
import com.inventrack.security.JwtAuthFilter;
import com.inventrack.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt, UserRepository users, JsonErrorWriter errors) throws Exception {
    JwtAuthFilter jwtFilter = new JwtAuthFilter(jwt, users, errors);
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(e -> e
            .authenticationEntryPoint((req, res, ex) -> errors.write(req, res, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión o tu sesión expiró"))
            .accessDeniedHandler((req, res, ex) -> errors.write(req, res, HttpStatus.FORBIDDEN, "No tienes permisos para esta operación")))
        .authorizeHttpRequests(a -> a
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            // Solo DUEÑO administra usuarios
            .requestMatchers("/api/v1/users/**").hasRole("DUENO")
            // DUEÑO y EMPLEADA registran entradas y salidas
            .requestMatchers(HttpMethod.POST, "/api/v1/stock-entries", "/api/v1/stock-exits").hasAnyRole("DUENO", "EMPLEADA")
            // Consultas: cualquier usuario autenticado
            .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
            .requestMatchers("/api/v1/auth/**").authenticated()
            // Crear, editar o eliminar productos, proveedores y lotes: solo DUEÑO
            .requestMatchers("/api/v1/**").hasRole("DUENO")
            .anyRequest().denyAll())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origins) {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(origins.stream().map(String::trim).toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-RqUID", "X-Channel", "X-ClientDt", "X-User-Id"));
    c.setAllowCredentials(false);
    c.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/api/**", c);
    return s;
  }
}
