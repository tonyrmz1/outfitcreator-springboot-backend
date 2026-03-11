package com.example.outfitcreator.documentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify API documentation accessibility.
 * Validates Requirements 14.1, 14.2, 14.3, 14.4
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiDocumentationAccessibilityTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldAccessSwaggerUiHtml() throws Exception {
        // Verify /swagger-ui.html is accessible (redirects to /swagger-ui/index.html)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/swagger-ui.html"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Should redirect (3xx) or return OK
        assertThat(response.statusCode()).isIn(200, 301, 302, 303, 307, 308);
    }

    @Test
    void shouldAccessSwaggerUiIndex() throws Exception {
        // Verify /swagger-ui/index.html is accessible
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/swagger-ui/index.html"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElse(""))
                .contains("text/html");
    }

    @Test
    void shouldAccessApiDocsJson() throws Exception {
        // Verify /v3/api-docs is accessible and returns JSON
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElse(""))
                .contains("application/json");
        
        String body = response.body();
        assertThat(body).isNotNull();
        assertThat(body).contains("\"openapi\"");
        assertThat(body).contains("\"OutfitCreator API\"");
        assertThat(body).contains("\"1.0.0\"");
    }

    @Test
    void shouldDocumentAllAuthenticationEndpoints() throws Exception {
        // Verify authentication endpoints are documented
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check all auth endpoints are documented
        assertThat(body).contains("\"/api/auth/register\"");
        assertThat(body).contains("\"/api/auth/login\"");
        assertThat(body).contains("\"/api/auth/profile\"");
    }

    @Test
    void shouldDocumentAllClothingItemEndpoints() throws Exception {
        // Verify clothing item endpoints are documented
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check all clothing item endpoints are documented
        assertThat(body).contains("\"/api/clothing\"");
        assertThat(body).contains("\"/api/clothing/{id}\"");
        assertThat(body).contains("\"/api/clothing/{id}/photo\"");
    }

    @Test
    void shouldDocumentAllOutfitEndpoints() throws Exception {
        // Verify outfit endpoints are documented
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check all outfit endpoints are documented
        assertThat(body).contains("\"/api/outfits\"");
        assertThat(body).contains("\"/api/outfits/{id}\"");
    }

    @Test
    void shouldDocumentRecommendationEndpoint() throws Exception {
        // Verify recommendation endpoint is documented
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        assertThat(body).contains("\"/api/recommendations\"");
    }

    @Test
    void shouldDocumentAuthenticationScheme() throws Exception {
        // Verify JWT Bearer authentication scheme is documented
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check security scheme is documented
        assertThat(body).contains("\"bearerAuth\"");
        assertThat(body).contains("\"type\":\"http\"");
        assertThat(body).contains("\"scheme\":\"bearer\"");
        assertThat(body).contains("\"bearerFormat\":\"JWT\"");
    }

    @Test
    void shouldApplySecurityRequirementToProtectedEndpoints() throws Exception {
        // Verify security requirements are applied to protected endpoints
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check that protected endpoints have security requirements
        // The API docs should show bearerAuth security for protected endpoints
        assertThat(body).contains("\"security\"");
        assertThat(body).contains("\"bearerAuth\"");
    }

    @Test
    void shouldIncludeRequestAndResponseSchemas() throws Exception {
        // Verify that endpoints include request/response schemas
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check that schemas are defined
        assertThat(body).contains("\"schemas\"");
        assertThat(body).contains("\"LoginRequest\"");
        assertThat(body).contains("\"LoginResponse\"");
        assertThat(body).contains("\"ClothingItemDTO\"");
        assertThat(body).contains("\"OutfitDTO\"");
    }

    @Test
    void shouldDocumentErrorResponses() throws Exception {
        // Verify that endpoints document error responses
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v3/api-docs"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(200);
        String body = response.body();
        assertThat(body).isNotNull();
        
        // Check that error responses are documented
        assertThat(body).contains("\"responses\"");
        assertThat(body).contains("\"401\""); // Unauthorized
        assertThat(body).contains("\"404\""); // Not Found
        assertThat(body).contains("\"403\""); // Forbidden
    }
}
