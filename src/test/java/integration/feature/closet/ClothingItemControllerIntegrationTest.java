package integration.feature.closet;

import com.example.outfitcreator.OutfitcreatorApplication;
import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import com.example.outfitcreator.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OutfitcreatorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ClothingItemController Integration Tests")
class ClothingItemControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ClothingItemRepository clothingItemRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private User testUser;
    private User anotherUser;
    private ClothingItem testItem;
    private String testUserToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        clothingItemRepository.deleteAll();
        userRepository.deleteAll();

        testUser = buildUser("test@example.com", "Test", "User");
        testUser = userRepository.save(testUser);

        anotherUser = buildUser("other@example.com", "Another", "User");
        anotherUser = userRepository.save(anotherUser);

        testItem = buildItem(testUser, "Blue Jeans", ClothingCategory.BOTTOM, "blue");
        testItem = clothingItemRepository.save(testItem);

        testUserToken = jwtUtil.generateToken(testUser.getId(), testUser.getEmail());
        anotherUserToken = jwtUtil.generateToken(anotherUser.getId(), anotherUser.getEmail());
    }

    private User buildUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setClothingItems(new ArrayList<>());
        user.setOutfits(new ArrayList<>());
        return user;
    }

    private ClothingItem buildItem(User user, String name, ClothingCategory category, String color) {
        ClothingItem item = new ClothingItem();
        item.setUser(user);
        item.setName(name);
        item.setPrimaryColor(color);
        item.setCategory(category);
        item.setSize("M");
        item.setSeason(Season.ALL_SEASON);
        item.setFitCategory(FitCategory.REGULAR);
        item.setWearCount(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    @Nested
    @DisplayName("GET /api/clothing")
    class GetAllTests {

        @Test
        @DisplayName("Should return 200 with paged items for authenticated user")
        void shouldReturn200WithItems() throws Exception {
            mockMvc.perform(get("/api/clothing")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("Blue Jeans"));
        }

        @Test
        @DisplayName("Should return 401 when request is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clothing"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should only return items belonging to the authenticated user")
        void shouldOnlyReturnOwnItems() throws Exception {
            ClothingItem anotherItem = buildItem(anotherUser, "Red Dress", ClothingCategory.TOP, "red");
            clothingItemRepository.save(anotherItem);

            mockMvc.perform(get("/api/clothing")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Blue Jeans"));
        }
    }

    @Nested
    @DisplayName("GET /api/clothing/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Should return 200 with item that belongs to authenticated user")
        void shouldReturnOwnItem() throws Exception {
            mockMvc.perform(get("/api/clothing/" + testItem.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Blue Jeans"))
                    .andExpect(jsonPath("$.category").value("BOTTOM"));
        }

        @Test
        @DisplayName("Should return 404 when item belongs to another user")
        void shouldReturn404ForAnotherUsersItem() throws Exception {
            mockMvc.perform(get("/api/clothing/" + testItem.getId())
                            .header("Authorization", "Bearer " + anotherUserToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when item does not exist")
        void shouldReturn404WhenItemDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/clothing/99999")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/clothing")
    class CreateItemTests {

        @Test
        @DisplayName("Should return 201 with created item when all required fields are provided")
        void shouldCreateItemWithoutPhoto() throws Exception {
            mockMvc.perform(multipart("/api/clothing")
                            .param("name", "White T-Shirt")
                            .param("primaryColor", "white")
                            .param("category", "TOP")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("White T-Shirt"))
                    .andExpect(jsonPath("$.primaryColor").value("white"))
                    .andExpect(jsonPath("$.category").value("TOP"));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            // Sends only an optional field — name, primaryColor, and category are missing
            mockMvc.perform(multipart("/api/clothing")
                            .param("brand", "Nike")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(multipart("/api/clothing")
                            .param("name", "White T-Shirt")
                            .param("primaryColor", "white")
                            .param("category", "TOP")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/clothing/{id}")
    class UpdateItemTests {

        @Test
        @DisplayName("Should return 200 with updated item data")
        void shouldUpdateItem() throws Exception {
            String requestBody = """
                    {
                      "name": "Dark Jeans",
                      "primaryColor": "dark blue",
                      "category": "BOTTOM"
                    }
                    """;

            mockMvc.perform(put("/api/clothing/" + testItem.getId())
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Dark Jeans"));
        }

        @Test
        @DisplayName("Should return 404 when item belongs to another user")
        void shouldReturn404ForAnotherUsersItem() throws Exception {
            String requestBody = """
                    {
                      "name": "Dark Jeans",
                      "primaryColor": "dark blue",
                      "category": "BOTTOM"
                    }
                    """;

            mockMvc.perform(put("/api/clothing/" + testItem.getId())
                            .header("Authorization", "Bearer " + anotherUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/clothing/{id}")
    class DeleteItemTests {

        @Test
        @DisplayName("Should return 204 when item is deleted successfully")
        void shouldReturn204OnSuccess() throws Exception {
            mockMvc.perform(delete("/api/clothing/" + testItem.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when item belongs to another user")
        void shouldReturn404ForAnotherUsersItem() throws Exception {
            mockMvc.perform(delete("/api/clothing/" + testItem.getId())
                            .header("Authorization", "Bearer " + anotherUserToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when item does not exist")
        void shouldReturn404WhenItemDoesNotExist() throws Exception {
            mockMvc.perform(delete("/api/clothing/99999")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNotFound());
        }
    }
}
