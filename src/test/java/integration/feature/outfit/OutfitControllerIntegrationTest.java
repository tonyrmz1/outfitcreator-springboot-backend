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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClothingItemRepository clothingItemRepository;

    @Autowired
    private FeatureOutfitRepository outfitRepository;

    @Autowired
    private OutfitItemRepository outfitItemRepository;

    private User testUser;
    private User anotherUser;
    private ClothingItem testTop;
    private ClothingItem testBottom;
    private ClothingItem testFootwear;
    private Outfit testOutfit;

    @BeforeEach
    void setUp() {
        outfitItemRepository.deleteAll();
        outfitRepository.deleteAll();
        clothingItemRepository.deleteAll();
        userRepository.deleteAll();

        testUser = createTestUser(1L, "test@example.com");
        anotherUser = createTestUser(2L, "another@example.com");
        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);

        testTop = createTestClothingItem(1L, testUser, "Blue Shirt", ClothingCategory.TOP, "blue");
        testBottom = createTestClothingItem(2L, testUser, "Black Pants", ClothingCategory.BOTTOM, "black");
        testFootwear = createTestClothingItem(3L, testUser, "White Sneakers", ClothingCategory.FOOTWEAR, "white");
        testTop = clothingItemRepository.save(testTop);
        testBottom = clothingItemRepository.save(testBottom);
        testFootwear = clothingItemRepository.save(testFootwear);

        testOutfit = createTestOutfit(1L, testUser);
        testOutfit = outfitRepository.save(testOutfit);
    }

    private User createTestUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setClothingItems(new ArrayList<>());
        user.setOutfits(new ArrayList<>());
        return user;
    }

    private ClothingItem createTestClothingItem(Long id, User user, String name, ClothingCategory category, String color) {
        ClothingItem item = new ClothingItem();
        item.setId(id);
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

    private Outfit createTestOutfit(Long id, User user) {
        Outfit outfit = new Outfit();
        outfit.setId(id);
        outfit.setUser(user);
        outfit.setName("Test Outfit");
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
        @WithMockUser(username = "1")
        void shouldCreateOutfitWithValidRequest() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("New Outfit")
                    .notes("Outfit notes")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(1L)
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(2L)
                                    .position(ItemPosition.BOTTOM)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/outfits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Outfit"))
                    .andExpect(jsonPath("$.isComplete").value(true));

            List<Outfit> outfits = outfitRepository.findAll();
            assertThat(outfits).hasSize(2);
        }

        @Test
        @DisplayName("Should return 400 for invalid request with missing name")
        @WithMockUser(username = "1")
        void shouldReturn400ForMissingName() throws Exception {
            String invalidRequest = "{\"items\": [{\"clothingItemId\": 1, \"position\": \"TOP\"}]}";

            mockMvc.perform(post("/api/outfits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty items list")
        @WithMockUser(username = "1")
        void shouldReturn400ForEmptyItems() throws Exception {
            String invalidRequest = "{\"name\": \"Empty Outfit\", \"items\": []}";

            mockMvc.perform(post("/api/outfits")
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
                                    .clothingItemId(1L)
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
        @WithMockUser(username = "1")
        void shouldReturnPaginatedOutfits() throws Exception {
            mockMvc.perform(get("/api/outfits")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("Test Outfit"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should return empty page when user has no outfits")
        @WithMockUser(username = "999")
        void shouldReturnEmptyPageWhenNoOutfits() throws Exception {
            mockMvc.perform(get("/api/outfits")
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
        @WithMockUser(username = "1")
        void shouldReturnOutfitByIdForOwner() throws Exception {
            mockMvc.perform(get("/api/outfits/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Outfit"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent outfit")
        @WithMockUser(username = "1")
        void shouldReturn404ForNonExistentOutfit() throws Exception {
            mockMvc.perform(get("/api/outfits/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/outfits/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/outfits/{id} - Update Outfit Tests")
    class UpdateOutfitTests {

        @Test
        @DisplayName("Should update outfit name and notes")
        @WithMockUser(username = "1")
        void shouldUpdateOutfitNameAndNotes() throws Exception {
            UpdateOutfitRequest request = UpdateOutfitRequest.builder()
                    .name("Updated Outfit Name")
                    .notes("Updated notes")
                    .build();

            mockMvc.perform(put("/api/outfits/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Outfit Name"))
                    .andExpect(jsonPath("$.notes").value("Updated notes"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent outfit")
        @WithMockUser(username = "1")
        void shouldReturn404ForNonExistentOutfit() throws Exception {
            UpdateOutfitRequest request = UpdateOutfitRequest.builder()
                    .name("Updated Name")
                    .build();

            mockMvc.perform(put("/api/outfits/999")
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

            mockMvc.perform(put("/api/outfits/1")
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
        @WithMockUser(username = "1")
        void shouldDeleteOutfitForOwner() throws Exception {
            mockMvc.perform(delete("/api/outfits/1"))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/outfits/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent outfit")
        @WithMockUser(username = "1")
        void shouldReturn404ForNonExistentOutfit() throws Exception {
            mockMvc.perform(delete("/api/outfits/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/outfits/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Database Interaction Tests")
    class DatabaseInteractionTests {

        @Test
        @DisplayName("Should persist outfit with clothing items")
        @WithMockUser(username = "1")
        void shouldPersistOutfitWithClothingItems() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Complete Outfit")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(1L)
                                    .position(ItemPosition.TOP)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(2L)
                                    .position(ItemPosition.BOTTOM)
                                    .build(),
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(3L)
                                    .position(ItemPosition.FOOTWEAR)
                                    .build()
                    ))
                    .build();

            MvcResult result = mockMvc.perform(post("/api/outfits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            OutfitDTO outfitDTO = objectMapper.readValue(responseBody, OutfitDTO.class);

            List<Outfit> outfits = outfitRepository.findAll();
            assertThat(outfits).hasSize(2);

            Outfit savedOutfit = outfits.stream()
                    .filter(o -> o.getName().equals("Complete Outfit"))
                    .findFirst()
                    .orElseThrow();

            assertThat(savedOutfit.getItems()).hasSize(3);
        }

        @Test
        @DisplayName("Should cascade delete outfit items when outfit is deleted")
        @WithMockUser(username = "1")
        void shouldCascadeDeleteOutfitItems() throws Exception {
            OutfitItem outfitItem = new OutfitItem();
            outfitItem.setOutfit(testOutfit);
            outfitItem.setClothingItem(testTop);
            outfitItem.setPosition(ItemPosition.TOP);
            outfitItemRepository.save(outfitItem);

            assertThat(outfitItemRepository.count()).isEqualTo(1);

            mockMvc.perform(delete("/api/outfits/1"))
                    .andExpect(status().isNoContent());

            assertThat(outfitItemRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should query outfits by user ID")
        @WithMockUser(username = "1")
        void shouldQueryOutfitsByUserId() throws Exception {
            Outfit secondOutfit = createTestOutfit(2L, testUser);
            secondOutfit.setName("Second Outfit");
            outfitRepository.save(secondOutfit);

            MvcResult result = mockMvc.perform(get("/api/outfits")
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
        @DisplayName("Should serialize and deserialize CreateOutfitRequest correctly")
        @WithMockUser(username = "1")
        void shouldSerializeAndDeserializeCreateOutfitRequest() throws Exception {
            CreateOutfitRequest request = CreateOutfitRequest.builder()
                    .name("Serialization Test")
                    .notes("Testing serialization")
                    .items(List.of(
                            CreateOutfitRequest.OutfitItemRequest.builder()
                                    .clothingItemId(1L)
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
        @DisplayName("Should serialize and deserialize OutfitDTO correctly")
        @WithMockUser(username = "1")
        void shouldSerializeAndDeserializeOutfitDTO() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/outfits/1"))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertThat(json).contains("\"id\":1");
            assertThat(json).contains("\"name\":\"Test Outfit\"");
            assertThat(json).contains("\"isComplete\":true");
        }
    }
}
