package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for API gateways unless needed
                .cors(corsSpec -> corsSpec.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.addAllowedOrigin("*"); // Allow all origins for simplicity; adjust as needed
                    corsConfig.addAllowedMethod("*"); // Allow all HTTP methods
                    corsConfig.addAllowedHeader("*"); // Allow all headers
                    return corsConfig;
                }))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll() // Allow public access to certain paths
                        .anyExchange().authenticated() // Require authentication for all other paths
                )
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> {
                    oAuth2ResourceServerSpec
                            .jwt(Customizer.withDefaults())
                            .authenticationEntryPoint((exchange, ex) -> {
                                String message = "Unauthorized: " + ex.getMessage();
                                byte[] bytes = String.format("{\"message\": \"%s\"}", message).getBytes();
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                            });
                });
        return httpSecurity.build();
    }
}
