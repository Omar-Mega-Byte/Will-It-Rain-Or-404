package com.weather_found.weather_app.modules.shared.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the Weather App
 * Configures authentication, authorization, JWT, and password encoding
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test")
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF protection
                                .csrf(csrf -> csrf.disable())

                                // Enable CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Configure exception handling
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                                // Configure authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints - no authentication required
                                                .requestMatchers("/").permitAll()
                                                .requestMatchers("/api/auth/register", "/api/auth/login",
                                                                "/api/auth/forgot-password", "/api/auth/reset-password")
                                                .permitAll()

                                                // Public documentation and health endpoints
                                                .requestMatchers("/actuator/health", "/v3/api-docs/**",
                                                                "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()

                                                // Public weather endpoint for home page
                                                .requestMatchers("/api/weather/random").permitAll()

                                                // Authentication required endpoints
                                                .requestMatchers("/api/auth/logout", "/api/auth/refresh")
                                                .authenticated()

                                                // User profile endpoints - USER role or higher
                                                .requestMatchers(HttpMethod.GET, "/api/users/profile")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/profile")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/users/preferences")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/preferences")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/users/account")
                                                .hasAnyRole("USER", "ADMIN")

                                                // Admin-only user management endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")

                                                // Weather endpoints - authenticated users (excluding random)
                                                .requestMatchers("/api/weather/**").hasAnyRole("USER", "ADMIN")

                                                // Location endpoints - authenticated users
                                                .requestMatchers("/api/locations/**").hasAnyRole("USER", "ADMIN")

                                                // Event endpoints - authenticated users
                                                .requestMatchers("/api/events/**").hasAnyRole("USER", "ADMIN")

                                                // Prediction endpoints - authenticated users
                                                .requestMatchers("/api/predictions/**").hasAnyRole("USER", "ADMIN")

                                                // Notification endpoints - authenticated users
                                                .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN")

                                                // Analytics endpoints - role-based access
                                                .requestMatchers(HttpMethod.GET, "/api/analytics/user")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers("/api/analytics/system", "/api/analytics/predictions")
                                                .hasRole("ADMIN")

                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())

                                // Configure session management (stateless for REST API)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(
                                                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS))

                                // Add JWT filter before username/password authentication filter
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
