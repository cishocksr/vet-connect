package com.vetconnect.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration
 *
 * CONFIGURES:
 * - JWT authentication filter
 * - CORS (Cross-Origin Resource Sharing)
 * - CSRF protection (disabled for REST APIs)
 * - Session management (stateless for JWT)
 * - Public vs protected endpoints
 * - Password encoding
 * - Authentication provider
 *
 * SECURITY MODEL:
 * - Stateless authentication (no server-side sessions)
 * - JWT tokens for authentication
 * - BCrypt password hashing
 * - CORS enabled for frontend access
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @Secured annotations
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure HTTP security
     *
     * This is the main security configuration method
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== CORS CONFIGURATION ==========
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ========== CSRF PROTECTION ==========
                // Disabled because we're using JWT (not cookies)
                // JWT tokens in Authorization header are not vulnerable to CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // ========== SESSION MANAGEMENT ==========
                // Stateless - no server-side sessions
                // Each request must include JWT token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ========== AUTHORIZATION RULES ==========
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS (no authentication required)

                        // Authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Health check endpoints
                        .requestMatchers("/actuator/health").permitAll()

                        // Public resource browsing (GET only)
                        .requestMatchers(HttpMethod.GET, "/api/resources/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // PROTECTED ENDPOINTS (authentication required)

                        // User profile management
                        .requestMatchers("/api/users/**").authenticated()

                        // Saved resources (must be logged in)
                        .requestMatchers("/api/saved/**").authenticated()

                        // Resource creation/modification (authenticated users only)
                        // In production, you might want to restrict this to ADMIN role
                        .requestMatchers(HttpMethod.POST, "/api/resources/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/resources/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/resources/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // ========== JWT FILTER ==========
                // Add our JWT filter before Spring Security's authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS configuration
     *
     * Allows frontend (running on different port/domain) to access API
     *
     * DEVELOPMENT:
     * - Allow localhost:5173 (Vite dev server)
     * - Allow localhost:3000 (React dev server)
     *
     * PRODUCTION:
     * - Configure actual frontend domain
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins (frontend URLs)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite
                "http://localhost:3000",  // React/Next.js
                "http://localhost:3001"   // Docs app
                // TODO: Add production domain
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Expose headers (headers that frontend can read)
        configuration.setExposedHeaders(List.of("Authorization"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age of pre-flight requests (in seconds)
        configuration.setMaxAge(3600L);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Password encoder bean
     *
     * Uses BCrypt hashing algorithm
     * - Automatically generates salt
     * - Adjustable strength (default: 10 rounds)
     * - Industry standard for password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider
     *
     * Configures how Spring Security authenticates users
     * - Uses our CustomUserDetailsService to load users
     * - Uses BCrypt to verify passwords
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean
     *
     * Required for manual authentication in AuthService
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}