package com.wastewatch.config;

import com.wastewatch.security.JwtAuthenticationEntryPoint;
import com.wastewatch.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "file://*", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Authenticated Auth Endpoints
                        .requestMatchers("/api/auth/me").authenticated()

                        // Location & Infrastructure Management (Phase 5)
                        // Read: ADMIN and VOLUNTEER
                        .requestMatchers(HttpMethod.GET, "/api/areas/**").hasAnyRole("ADMIN", "VOLUNTEER")
                        .requestMatchers(HttpMethod.GET, "/api/buildings/**").hasAnyRole("ADMIN", "VOLUNTEER")
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").hasAnyRole("ADMIN", "VOLUNTEER")

                        // Write / Update / Delete: ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/areas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/areas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/areas/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/buildings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/buildings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/buildings/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")

                        // Inspections & Audits (Phase 6)
                        .requestMatchers("/api/inspections/**").hasAnyRole("ADMIN", "VOLUNTEER")

                        // Violations Workflow (Phase 6)
                        .requestMatchers("/api/violations/**").hasRole("ADMIN")

                        // Executive Dashboard & Analytics Engine (Phase 6, Phase 10)
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                        .requestMatchers("/api/analytics/**").hasRole("ADMIN")

                        // Map & Geolocation (Phase 6)
                        .requestMatchers("/api/map/**").hasAnyRole("ADMIN", "VOLUNTEER")

                        // Volunteer Management (Phase 6)
                        .requestMatchers("/api/volunteers/**").authenticated()

                        // In-App Notifications (Phase 13)
                        .requestMatchers("/api/notifications/**").authenticated()

                        // Reporting and Governance (Phase 14)
                        .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // Any other API requires authentication
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
