package com.example.outfitcreator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void shouldConfigureOpenAPIWithCorrectInfo() {
        // Verify API info
        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("OutfitCreator API");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
        assertThat(info.getDescription()).contains("Digital wardrobe management");
        assertThat(info.getDescription()).contains("outfit recommendation");
    }

    @Test
    void shouldConfigureJWTBearerSecurityScheme() {
        // Verify security scheme
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(securityScheme.getDescription()).contains("JWT Bearer token");
    }

    @Test
    void shouldAddSecurityRequirement() {
        // Verify security requirement is added globally
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).isNotEmpty();
        
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertThat(securityRequirement.containsKey("bearerAuth")).isTrue();
    }
}
