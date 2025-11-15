package org.avyla.shared.config;

import lombok.RequiredArgsConstructor;
import org.avyla.security.application.service.UserDetailServiceImpl;
import org.avyla.security.config.filter.JwtTokenValidator;
import org.avyla.shared.util.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity // ← Importante para @PreAuthorize
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
                    // Solo login (sign-up comentado para producción)
                    http.requestMatchers(HttpMethod.POST, "/api/auth/log-in").permitAll();
                    // http.requestMatchers(HttpMethod.POST, "/api/auth/sing-up").permitAll(); // ← Comentar en producción

                    // ========== USERS - REGLAS ESPECÍFICAS PRIMERO ==========
                    // Perfil actual (todos los autenticados)
                    http.requestMatchers(HttpMethod.GET, "/api/users/me").authenticated();

                    // Búsqueda de usuarios (ADMIN y SUPERVISOR)
                    http.requestMatchers(HttpMethod.GET, "/api/users/search").hasAnyRole("ADMIN", "SUPERVISOR");

                    // Crear usuario (solo ADMIN)
                    http.requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN");

                    // Actualizar usuario (solo ADMIN)
                    http.requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN");

                    // Cambiar contraseña (solo ADMIN)
                    http.requestMatchers(HttpMethod.PATCH, "/api/users/*/password").hasRole("ADMIN");

                    // Eliminar usuario (solo ADMIN)
                    http.requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN");

                    // Restaurar usuario (solo ADMIN)
                    http.requestMatchers(HttpMethod.POST, "/api/users/*/restore").hasRole("ADMIN");

                    // Listar y ver detalle (ADMIN y SUPERVISOR) - MÁS GENÉRICO AL FINAL
                    http.requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "SUPERVISOR");

                    // ========== VEHICLES ==========
                    http.requestMatchers(HttpMethod.GET, "/api/vehicles/published").permitAll();
                    http.requestMatchers(HttpMethod.POST, "/api/vehicles").hasAnyRole("FLEET_MANAGER", "ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/vehicles/*").hasAnyRole("FLEET_MANAGER", "ADMIN");
                    http.requestMatchers(HttpMethod.DELETE, "/api/vehicles/*").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/vehicles/*/activate").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/vehicles/*/documents").hasAnyRole("FLEET_MANAGER", "ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/vehicles/*/documents/**").authenticated();
                    http.requestMatchers(HttpMethod.GET, "/api/vehicles/**").authenticated();

                    // ========== CHECKLISTS ==========
                    http.requestMatchers(HttpMethod.GET, "/api/checklists/templates/*/versions/published").permitAll();
                    http.requestMatchers(HttpMethod.GET, "/api/checklists/me/**").hasRole("DRIVER");
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/instances/*/responses").hasAnyRole("DRIVER", "MECHANIC");
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/instances/*/submit").hasAnyRole("DRIVER", "MECHANIC");
                    http.requestMatchers(HttpMethod.GET, "/api/checklists/instances/**").hasAnyRole("SUPERVISOR", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/instances").hasAnyRole("SUPERVISOR", "ADMIN");

                    // Attachments
                    http.requestMatchers(HttpMethod.POST, "/api/checklists/*/attachments").hasAnyRole("DRIVER", "MECHANIC");
                    http.requestMatchers(HttpMethod.GET, "/api/attachments/**").authenticated();
                    http.requestMatchers(HttpMethod.DELETE, "/api/attachments/**").hasAnyRole("SUPERVISOR", "ADMIN");

                    // ========== ADMIN ==========
                    http.requestMatchers("/api/admin/**").hasRole("ADMIN");

                    // ========== DEFAULT ==========
                    http.anyRequest().authenticated();
                })
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
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