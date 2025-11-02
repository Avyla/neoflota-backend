package org.avyla.security.config;

import lombok.RequiredArgsConstructor;
import org.avyla.security.application.service.UserDetailServiceImpl;
import org.avyla.security.config.filter.JwtTokenValidator;
import org.avyla.security.infraestructure.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationProvider authenticationProvider) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {

                    // ========== ENDPOINTS PÚBLICOS ==========
                    http.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll();

                    // ✅ IMPORTANTE: Reglas ESPECÍFICAS primero
                    http.requestMatchers(HttpMethod.GET, "/api/vehicles/published").permitAll();
                    http.requestMatchers(HttpMethod.GET, "/api/checklists/templates/*/versions/published").permitAll();

                    // ========== CHECKLISTS ==========
                    http.requestMatchers(HttpMethod.GET, "/api/checklists/me/**").hasRole("DRIVER");
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/instances/*/responses").hasAnyRole("DRIVER", "MECHANIC");
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/instances/*/submit").hasAnyRole("DRIVER", "MECHANIC");
                    http.requestMatchers(HttpMethod.GET, "/api/checklists/instances/**").hasAnyRole("SUPERVISOR", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/instances").hasAnyRole("SUPERVISOR", "ADMIN");

                    // Attachments
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/*/attachments").hasAnyRole("DRIVER", "MECHANIC");
                    http.requestMatchers(HttpMethod.GET, "/api/attachments/**").authenticated();
                    http.requestMatchers(HttpMethod.DELETE, "/api/attachments/**").hasAnyRole("SUPERVISOR", "ADMIN");

                    // ========== VEHICLES ==========
                    // ⚠️ CRITICAL: Reglas específicas ANTES de las genéricas
                    http.requestMatchers(HttpMethod.POST, "/api/vehicles").hasAnyRole("FLEET_MANAGER", "ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/vehicles/*").hasAnyRole("FLEET_MANAGER", "ADMIN");
                    http.requestMatchers(HttpMethod.DELETE, "/api/vehicles/*").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/vehicles/*/activate").hasRole("ADMIN");

                    // Documentos (específicos primero)
                    http.requestMatchers(HttpMethod.POST, "/api/vehicles/*/documents").hasAnyRole("FLEET_MANAGER", "ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/vehicles/*/documents/**").authenticated();

                    // ✅ Regla GENÉRICA al final
                    http.requestMatchers(HttpMethod.GET, "/api/vehicles/**").authenticated();

                    // ========== ADMIN ==========
                    http.requestMatchers("/api/admin/**").hasRole("ADMIN");

                    // ========== DEFAULT ==========
                    http.anyRequest().authenticated();
                })
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
