package integration.feature.auth;

import com.example.outfitcreator.OutfitcreatorApplication;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.feature.auth.dto.request.LoginRequest;
import com.example.outfitcreator.feature.auth.dto.request.RegisterRequest;
import com.example.outfitcreator.feature.auth.dto.request.UpdateProfileRequest;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import com.example.outfitcreator.infrastructure.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OutfitcreatorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private User testUser;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setClothingItems(new ArrayList<>());
        testUser.setOutfits(new ArrayList<>());
        testUser = userRepository.save(testUser);

        testUserToken = jwtUtil.generateToken(testUser.getId(), testUser.getEmail());
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should return 200 with user data when registration succeeds")
        void shouldRegisterNewUser() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("password123");
            request.setFirstName("Jane");
            request.setLastName("Smith");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("");
            request.setPassword("");
            request.setFirstName("");
            request.setLastName("");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is already registered")
        void shouldReturn400WhenEmailAlreadyInUse() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");
            request.setFirstName("John");
            request.setLastName("Doe");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 with a JWT token on valid credentials")
        void shouldReturnTokenOnValidCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Should return 401 when password is wrong")
        void shouldReturn401OnWrongPassword() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("wrongpassword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 when user does not exist")
        void shouldReturn401WhenUserNotFound() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("unknown@example.com");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/profile")
    class GetProfileTests {

        @Test
        @DisplayName("Should return 200 with profile data for an authenticated user")
        void shouldReturnProfileWhenAuthenticated() throws Exception {
            mockMvc.perform(get("/api/auth/profile")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            mockMvc.perform(get("/api/auth/profile"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should return 200 with updated profile data")
        void shouldReturnUpdatedProfile() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            mockMvc.perform(put("/api/auth/profile")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            mockMvc.perform(put("/api/auth/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when first name is blank")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("");
            request.setLastName("Smith");

            mockMvc.perform(put("/api/auth/profile")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
