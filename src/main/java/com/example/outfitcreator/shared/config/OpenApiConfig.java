package com.example.outfitcreator.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("OutfitCreator API")
                        .version("1.0.0")
                        .description("Digital wardrobe management and intelligent outfit recommendation API. " +
                                "This API allows users to manage their clothing items, create outfits, " +
                                "and receive personalized outfit recommendations based on color theory, " +
                                "fit compatibility, and seasonal appropriateness."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token authentication. " +
                                        "Obtain a token by calling POST /api/auth/login with valid credentials. " +
                                        "Include the token in the Authorization header as: Bearer <token>")));
    }
}
