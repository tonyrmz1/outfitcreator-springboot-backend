package integration.feature.outfit;

import com.example.outfitcreator.OutfitcreatorApplication;
import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.Outfit;
import com.example.outfitcreator.core.entity.OutfitItem;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.ItemPosition;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.outfit.dto.request.CreateOutfitRequest;
import com.example.outfitcreator.feature.outfit.dto.response.OutfitDTO;
import com.example.outfitcreator.feature.outfit.dto.request.UpdateOutfitRequest;
import com.example.outfitcreator.feature.closet.repository.ClothingItemRepository;
import com.example.outfitcreator.feature.outfit.repository.OutfitItemRepository;
import com.example.outfitcreator.feature.outfit.repository.FeatureOutfitRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = OutfitcreatorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OutfitControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ClothingItemRepository clothingItemRepository;
    @Autowired private FeatureOutfitRepository outfitRepository;
    @Autowired private OutfitItemRepository outfitItemRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private User testUser;
    private User anotherUser;
    private ClothingItem testTop;
    private ClothingItem testBottom;
    private ClothingItem testFootwear;
    private Outfit testOutfit;
    private String testUserToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        outfitItemRepository.deleteAll();
        outfitRepository.deleteAll();
        clothingItemRepository.deleteAll();
        userRepository.deleteAll();

        testUser = buildUser("test@example.com");
        anotherUser = buildUser("another@example.com");
        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);

        testTop = buildItem(testUser, "Blue Shirt", ClothingCategory.TOP, "blue");
        testBottom = buildItem(testUser, "Black Pants", ClothingCategory.BOTTOM, "black");
        testFootwear = buildItem(testUser, "White Sneakers", ClothingCategory.FOOTWEAR, "white");
        testTop = clothingItemRepository.save(testTop);
        testBottom = clothingItemRepository.save(testBottom);
        testFootwear = clothingItemRepository.save(testFootwear);

        testOutfit = buildOutfit(testUser, "Test Outfit");
        testOutfit = outfitRepository.save(testOutfit);

        testUserToken = jwtUtil.generateToken(testUser.getId(), testUser.getEmail());
        anotherUserToken = jwtUtil.generateToken(anotherUser.getId(), anotherUser.getEmail());
    }

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Test");
        user.setLastName("User");
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

    private Outfit buildOutfit(User user, String name) {
        Outfit outfit = new Outfit();
        outfit.setUser(user);
        outfit.setName(name);
        outfit.setNotes("Test notes");
        outfit.setIsComplete(true);
        outfit.setItems(new ArrayList<>());
        outfit.setCreatedAt(LocalDateTime.now());
        outfit.setUpdatedAt(LocalDateTime.now());
        return outfit;
    }

    @Nested
    @DisplayName("POST /api/outfits - Create Outfit Tests")
    class CreateOutfitTests {

        @Test
        @DisplayName("Should create outfit with valid request")
        void shouldCreateOutfitWithValidRequest() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("New Outfit")
                    .notes("Outfit notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/outfits")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Outfit"))
                    .andExpect(jsonPath("$.isComplete").value(true));

            assertThat(outfitRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return 400 for invalid request with missing name")
        void shouldReturn400ForMissingName() throws Exception {
            String invalidRequest = """
                    {"items": [{"clothingItemId": 1, "position": "TOP"}]}
                    """;

            mockMvc.perform(post("/api/outfits")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty items list")
        void shouldReturn400ForEmptyItems() throws Exception {
            String invalidRequest = """
                    {"name": "Empty Outfit", "items": []}
                    """;

            mockMvc.perform(post("/api/outfits")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Unauthorized Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/outfits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/outfits - Get All Outfits Tests")
    class GetAllOutfitsTests {

        @Test
        @DisplayName("Should return paginated outfits for user")
        void shouldReturnPaginatedOutfits() throws Exception {
            mockMvc.perform(get("/api/outfits")
                            .header("Authorization", "Bearer " + testUserToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("Test Outfit"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should return empty page when user has no outfits")
        void shouldReturnEmptyPageWhenNoOutfits() throws Exception {
            mockMvc.perform(get("/api/outfits")
                            .header("Authorization", "Bearer " + anotherUserToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/outfits"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/outfits/{id} - Get Outfit By ID Tests")
    class GetOutfitByIdTests {

        @Test
        @DisplayName("Should return outfit by ID for owner")
        void shouldReturnOutfitByIdForOwner() throws Exception {
            mockMvc.perform(get("/api/outfits/" + testOutfit.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testOutfit.getId()))
                    .andExpect(jsonPath("$.name").value("Test Outfit"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent outfit")
        void shouldReturn404ForNonExistentOutfit() throws Exception {
            mockMvc.perform(get("/api/outfits/99999")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/outfits/" + testOutfit.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/outfits/{id} - Update Outfit Tests")
    class UpdateOutfitTests {

        @Test
        @DisplayName("Should update outfit name and notes")
        void shouldUpdateOutfitNameAndNotes() throws Exception {
            UpdateOutfitRequest request = UpdateOutfitRequest.builder()
                    .name("Updated Outfit Name")
                    .notes("Updated notes")
                    .build();

            mockMvc.perform(put("/api/outfits/" + testOutfit.getId())
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Outfit Name"))
                    .andExpect(jsonPath("$.notes").value("Updated notes"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent outfit")
        void shouldReturn404ForNonExistentOutfit() throws Exception {
            UpdateOutfitRequest request = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .build();

            mockMvc.perform(put("/api/outfits/99999")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            UpdateOutfitRequest request = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .build();

            mockMvc.perform(put("/api/outfits/" + testOutfit.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/outfits/{id} - Delete Outfit Tests")
    class DeleteOutfitTests {

        @Test
        @DisplayName("Should delete outfit for owner")
        void shouldDeleteOutfitForOwner() throws Exception {
            mockMvc.perform(delete("/api/outfits/" + testOutfit.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/outfits/" + testOutfit.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent outfit")
        void shouldReturn404ForNonExistentOutfit() throws Exception {
            mockMvc.perform(delete("/api/outfits/99999")
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/outfits/" + testOutfit.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Database Interaction Tests")
    class DatabaseInteractionTests {

        @Test
        @DisplayName("Should persist outfit with clothing items")
        void shouldPersistOutfitWithClothingItems() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Complete Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testBottom.getId())
                                    .position(ItemPosition.BOTTOM)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testFootwear.getId())
                                    .position(ItemPosition.FOOTWEAR)
                                    .build()
                    ))
                    .build();

            MvcResult result = mockMvc.perform(post("/api/outfits")
                            .header("Authorization", "Bearer " + testUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            OutfitDTO outfitDTO = objectMapper.readValue(responseBody, OutfitDTO.class);

            assertThat(outfitRepository.count()).isEqualTo(2);
            assertThat(outfitDTO.getItems()).hasSize(3);
        }

        @Test
        @DisplayName("Should cascade delete outfit items when outfit is deleted")
        void shouldCascadeDeleteOutfitItems() throws Exception {
            OutfitItem outfitItem = new OutfitItem();
            outfitItem.setOutfit(testOutfit);
            outfitItem.setClothingItem(testTop);
            outfitItem.setPosition(ItemPosition.TOP);
            outfitItemRepository.save(outfitItem);

            assertThat(outfitItemRepository.count()).isEqualTo(1);

            mockMvc.perform(delete("/api/outfits/" + testOutfit.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isNoContent());

            assertThat(outfitItemRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should query outfits by user ID")
        void shouldQueryOutfitsByUserId() throws Exception {
            Outfit secondOutfit = buildOutfit(testUser, "Second Outfit");
            outfitRepository.save(secondOutfit);

            MvcResult result = mockMvc.perform(get("/api/outfits")
                            .header("Authorization", "Bearer " + testUserToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("Test Outfit");
            assertThat(responseBody).contains("Second Outfit");
        }
    }

    @Nested
    @DisplayName("Request/Response Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize CreateOutfitRequest correctly")
        void shouldSerializeCreateOutfitRequest() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Serialization Test")
                    .notes("Testing serialization")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(testTop.getId())
                                    .position(ItemPosition.TOP)
                                    .build()
                    ))
                    .build();

            String json = objectMapper.writeValueAsString(request);
            assertThat(json).contains("\"name\":\"Serialization Test\"");
            assertThat(json).contains("\"items\"");
            assertThat(json).contains("\"position\":\"TOP\"");
        }

        @Test
        @DisplayName("Should deserialize OutfitDTO from response correctly")
        void shouldSerializeAndDeserializeOutfitDTO() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/outfits/" + testOutfit.getId())
                            .header("Authorization", "Bearer " + testUserToken))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertThat(json).contains("\"id\":" + testOutfit.getId());
            assertThat(json).contains("\"name\":\"Test Outfit\"");
            assertThat(json).contains("\"isComplete\":true");
        }
    }
}
