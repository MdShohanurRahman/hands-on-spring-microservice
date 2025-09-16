package com.example.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("User Service API")
                                .version("1.0")
                                .description("Documentation for User Service API")
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addServersItem(
                        new Server()
                                .url("http://localhost:8080")
                );
    }

    /*@Bean
    public OpenAPI customOpenAPIWithOAuth2() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("User Service API")
                                .version("1.0")
                                .description("Documentation for User Service API")
                )
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "oauth2",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .flows(
                                                        new OAuthFlows()
                                                                .authorizationCode(
                                                                        new OAuthFlow()
                                                                                .authorizationUrl("https://dev-1spgkyzchsjzz7md.us.auth0.com/authorize")
                                                                                .tokenUrl("https://dev-1spgkyzchsjzz7md.us.auth0.com/oauth/token")
                                                                                .scopes(new Scopes()
                                                                                        .addString("read:all", "Read all data")
                                                                                        .addString("write:all", "Write data")
                                                                                )
                                                                )

                                                )
                                )
                )
                .addServersItem(
                        new Server()
                                .url("http://localhost:8080")
                );

    }*/
}
