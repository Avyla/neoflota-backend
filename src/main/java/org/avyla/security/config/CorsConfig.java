package org.avyla.security.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Bean que lee la configuración CORS desde application.yml
     */
    @Bean
    @ConfigurationProperties(prefix = "cors")
    public CorsProperties corsProperties() {
        return new CorsProperties();
    }

    /**
     * Configura CORS basándose en las propiedades del archivo yml
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProps) {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (lista blanca)
        configuration.setAllowedOrigins(corsProps.getAllowedOrigins());

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(corsProps.getAllowedMethods());

        // Headers que el cliente puede enviar
        configuration.setAllowedHeaders(corsProps.getAllowedHeaders());

        // Headers que el servidor expone al cliente
        configuration.setExposedHeaders(corsProps.getExposedHeaders());

        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(corsProps.getAllowCredentials());

        // Tiempo que el navegador cachea la respuesta preflight (OPTIONS)
        configuration.setMaxAge(corsProps.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todos los endpoints

        return source;
    }

    /**
     * Clase interna para mapear las propiedades del yml
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CorsProperties {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private Boolean allowCredentials = false;
        private Long maxAge = 3600L;
    }
}