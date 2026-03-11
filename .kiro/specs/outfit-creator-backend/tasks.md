# Implementation Plan: OutfitCreator Backend

## Overview

This implementation plan breaks down the OutfitCreator Spring Boot backend into incremental, testable steps. The application provides digital wardrobe management and intelligent outfit recommendations using color theory and fit analysis. Each task builds on previous work, with property-based tests using jqwik to validate correctness properties throughout development.

## Tasks

- [x] 1. Project setup and core infrastructure
  - [x] 1.1 Initialize Spring Boot project with Maven
    - Create Spring Boot 3.x project with dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL, H2, Lombok, Validation
    - Add jqwik dependency for property-based testing (version 1.7.4)
    - Add Thumbnailator for image processing
    - Add SpringDoc OpenAPI for API documentation
    - Add JJWT for JWT token handling
    - Configure application.properties for dev and test profiles
    - _Requirements: 13.1, 14.1_
  
  - [x] 1.2 Create database schema and entity models
    - Create User entity with email, password, firstName, lastName, timestamps
    - Create ClothingItem entity with all attributes (name, brand, colors, category, size, season, fitCategory, purchaseDate, photoPath, wearCount)
    - Create Outfit entity with name, notes, isComplete flag
    - Create OutfitItem entity (junction table) with position field
    - Create AuditLog entity for tracking modifications
    - Define enums: ClothingCategory, Season, FitCategory, ItemPosition, ColorHarmonyType
    - Add JPA annotations, relationships, and indexes
    - _Requirements: 3.1, 4.5, 13.1, 13.2, 13.3_
  
  - [x] 1.3 Create JPA repositories
    - Create UserRepository with findByEmail method
    - Create ClothingItemRepository with findByUserId, filtering methods (by category, season, color)
    - Create OutfitRepository with findByUserId
    - Create AuditLogRepository
    - _Requirements: 13.1, 13.2, 13.3_

- [x] 2. Security and authentication implementation
  - [x] 2.1 Implement JWT utility and security configuration
    - Create JwtUtil class with generateToken, extractUserId, extractEmail, validateToken methods
    - Create JwtAuthenticationFilter extending OncePerRequestFilter
    - Create SecurityConfig with BCrypt password encoder, security filter chain, CORS configuration
    - Configure stateless session management
    - _Requirements: 1.1, 1.2_
  
  - [x] 2.2 Implement AuthService and AuthController
    - Create DTOs: RegisterRequest, LoginRequest, LoginResponse, UserDTO, UpdateProfileRequest
    - Implement AuthService with register, login, getProfile, updateProfile methods
    - Implement AuthController with POST /api/auth/register, POST /api/auth/login, GET /api/auth/profile, PUT /api/auth/profile endpoints
    - Add password encryption with BCrypt
    - Add validation annotations to request DTOs
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  
  - [ ]* 2.3 Write property tests for authentication
    - **Property 1: User Registration Round-Trip**
    - **Validates: Requirements 1.1, 1.2**
    - **Property 2: Digital Closet Isolation**
    - **Validates: Requirements 1.3**
    - **Property 3: Profile Update Round-Trip**
    - **Validates: Requirements 1.4**
    - **Property 4: Authentication Failure Returns 401**
    - **Validates: Requirements 1.5**
  
  - [ ]* 2.4 Write unit tests for authentication
    - Test invalid credentials return 401
    - Test JWT token generation and validation
    - Test password encryption
    - Test user registration with duplicate email
    - _Requirements: 1.1, 1.2, 1.5_

- [x] 3. Photo storage and management
  - [x] 3.1 Implement PhotoService
    - Create PhotoService with uploadPhoto, deletePhoto, getPhoto, resizeImage methods
    - Implement file type validation (JPEG, PNG, GIF only)
    - Implement file size validation (max 5MB)
    - Implement unique filename generation using item ID and timestamp
    - Implement image resizing to max 1920x1080 using Thumbnailator
    - Implement thumbnail generation (300x300)
    - Configure storage base path in application.properties
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 12.1, 12.2, 12.3, 12.4, 12.5_
  
  - [x] 3.2 Create PhotoUrlService and photo endpoint
    - Create PhotoUrlService with generatePhotoUrl and generateThumbnailUrl methods
    - Create PhotoController with GET /api/photos/{filename} endpoint to serve images
    - _Requirements: 2.1, 12.1_
  
  - [ ]* 3.3 Write property tests for photo storage
    - **Property 6: Photo Upload Round-Trip**
    - **Validates: Requirements 2.1, 2.2**
    - **Property 7: Invalid File Type Rejection**
    - **Validates: Requirements 2.4**
    - **Property 8: Oversized File Rejection**
    - **Validates: Requirements 2.5**
    - **Property 33: Unique Filename Generation**
    - **Validates: Requirements 12.1**
    - **Property 34: Image Resolution Constraint**
    - **Validates: Requirements 12.2, 12.4**
    - **Property 35: Photo Replacement Cleanup**
    - **Validates: Requirements 12.3**
  
  - [ ]* 3.4 Write unit tests for photo service
    - Test file type validation with various formats
    - Test file size validation with edge cases
    - Test image resizing with different dimensions
    - Test thumbnail generation
    - Test storage failure handling
    - _Requirements: 2.2, 2.3, 2.4, 2.5, 12.2, 12.4_

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Clothing item management
  - [x] 5.1 Implement ClothingItemService
    - Create DTOs: CreateClothingItemRequest, UpdateClothingItemRequest, ClothingItemDTO, ClothingItemFilter
    - Implement create method with photo upload integration
    - Implement update method with attribute validation
    - Implement delete method with outfit reference checking
    - Implement getById with user ownership verification
    - Implement findAll with pagination and filtering (category, season, color)
    - Implement audit logging for all modifications
    - Add validation for category enum values
    - _Requirements: 2.1, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 5.2 Implement ClothingItemController
    - Create POST /api/clothing endpoint with multipart file support
    - Create GET /api/clothing endpoint with pagination and filter query parameters
    - Create GET /api/clothing/{id} endpoint
    - Create PUT /api/clothing/{id} endpoint
    - Create DELETE /api/clothing/{id} endpoint
    - Create POST /api/clothing/{id}/photo endpoint for photo replacement
    - Add @Valid annotations for request validation
    - Extract userId from SecurityContext
    - _Requirements: 2.1, 3.1, 3.4, 3.5, 4.1, 4.2, 4.4, 5.1, 5.2, 5.3, 5.5_
  
  - [ ]* 5.3 Write property tests for clothing item management
    - **Property 5: Clothing Item Creation Round-Trip**
    - **Validates: Requirements 3.1, 3.3**
    - **Property 9: Category Validation**
    - **Validates: Requirements 3.4, 3.5**
    - **Property 10: Clothing Item Update Preserves ID**
    - **Validates: Requirements 4.1**
    - **Property 11: Clothing Item Deletion**
    - **Validates: Requirements 4.2, 4.4**
    - **Property 12: Audit Log Creation**
    - **Validates: Requirements 4.5**
    - **Property 13: Category Filter Correctness**
    - **Validates: Requirements 5.1**
    - **Property 14: Season Filter Correctness**
    - **Validates: Requirements 5.2**
    - **Property 15: Color Filter Correctness**
    - **Validates: Requirements 5.3**
    - **Property 16: Pagination Bounds**
    - **Validates: Requirements 5.5**
  
  - [ ]* 5.4 Write unit tests for clothing item service
    - Test create with valid and invalid attributes
    - Test update with non-existent item (404)
    - Test delete with outfit references
    - Test user ownership verification (403)
    - Test filtering by category, season, color
    - Test pagination edge cases
    - _Requirements: 3.4, 3.5, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.5_

- [x] 6. Outfit management
  - [x] 6.1 Implement OutfitService
    - Create DTOs: CreateOutfitRequest, UpdateOutfitRequest, OutfitDTO, OutfitItemDTO
    - Implement create method with clothing item validation
    - Implement update method for name and notes
    - Implement delete method
    - Implement getById with user ownership verification
    - Implement findAll with pagination
    - Implement handleClothingItemDeletion to mark outfits as incomplete
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [x] 6.2 Implement OutfitController
    - Create POST /api/outfits endpoint
    - Create GET /api/outfits endpoint with pagination
    - Create GET /api/outfits/{id} endpoint
    - Create PUT /api/outfits/{id} endpoint
    - Create DELETE /api/outfits/{id} endpoint
    - Extract userId from SecurityContext
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [ ]* 6.3 Write property tests for outfit management
    - **Property 17: Outfit Creation Round-Trip**
    - **Validates: Requirements 6.1, 6.2**
    - **Property 18: Outfit ID and Timestamp Generation**
    - **Validates: Requirements 6.3**
    - **Property 19: Outfit Update Round-Trip**
    - **Validates: Requirements 6.4**
    - **Property 20: Outfit Incompleteness on Item Deletion**
    - **Validates: Requirements 6.5**
  
  - [ ]* 6.4 Write unit tests for outfit service
    - Test create with invalid clothing item references
    - Test update with non-existent outfit
    - Test delete cascade behavior
    - Test outfit incompleteness when item deleted
    - Test user ownership verification
    - _Requirements: 6.1, 6.2, 6.4, 6.5_

- [x] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Recommendation engine - Color compatibility
  - [x] 8.1 Implement ColorWheel utility
    - Create ColorWheel class with color-to-hue mapping
    - Implement getHue method for color name to degree conversion
    - Implement isNeutral method for neutral color detection
    - Map colors: red(0°), orange(30°), yellow(60°), lime(90°), green(120°), cyan(180°), blue(240°), purple(270°), magenta(300°), pink(330°), brown(25°)
    - Map neutrals: white, black, gray, beige (-1)
    - _Requirements: 8.1, 8.2, 8.3_
  
  - [x] 8.2 Implement color compatibility algorithm
    - Create RecommendationEngine service class
    - Implement calculateColorCompatibility method
    - Handle neutral colors (always 95.0 score)
    - Calculate hue difference with wrap-around (max 180°)
    - Score monochromatic/analogous (0-30°): 90.0
    - Score complementary (150-210°): 85.0
    - Score triadic (110-130°): 80.0
    - Score analogous extended (50-70°): 75.0
    - Score other combinations: 50.0
    - Use primary color only for matching
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [ ]* 8.3 Write property tests for color compatibility
    - **Property 22: Primary Color Matching Priority**
    - **Validates: Requirements 8.2**
    - **Property 23: Color Harmony Recognition**
    - **Validates: Requirements 8.3**
    - **Property 24: Color Compatibility Score Range**
    - **Validates: Requirements 8.5, 18.3**
  
  - [ ]* 8.4 Write unit tests for color compatibility
    - Test complementary colors (red-cyan, blue-orange)
    - Test analogous colors (blue-cyan-green)
    - Test monochromatic colors (blue-blue)
    - Test neutral combinations (black-red, white-blue)
    - Test score ranges
    - _Requirements: 8.1, 8.3, 8.5_

- [x] 9. Recommendation engine - Fit compatibility
  - [x] 9.1 Implement fit compatibility algorithm
    - Implement calculateFitCompatibility method in RecommendationEngine
    - Score tight-tight: 30.0 (avoid)
    - Score loose-loose: 40.0 (avoid)
    - Score oversized-oversized: 20.0 (avoid)
    - Score tight-loose or loose-tight: 95.0 (excellent balance)
    - Score tight-regular or regular-tight: 90.0 (good balance)
    - Score loose-regular or regular-loose: 85.0 (good balance)
    - Score regular-regular: 80.0 (safe)
    - Score other combinations: 70.0
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [ ]* 9.2 Write property tests for fit compatibility
    - **Property 25: Tight-Loose Fit Pairing**
    - **Validates: Requirements 9.2**
    - **Property 26: Loose-Tight Fit Pairing**
    - **Validates: Requirements 9.3**
    - **Property 27: Invalid Fit Combination Exclusion**
    - **Validates: Requirements 9.4**
    - **Property 28: Fit Compatibility Score Range**
    - **Validates: Requirements 9.5**
  
  - [ ]* 9.3 Write unit tests for fit compatibility
    - Test all fit category combinations
    - Test tight-tight rejection
    - Test loose-loose rejection
    - Test optimal pairings (tight-loose)
    - Test score ranges
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 10. Recommendation engine - Seasonal appropriateness
  - [x] 10.1 Implement seasonal appropriateness logic
    - Implement isSeasonallyAppropriate method in RecommendationEngine
    - Implement areAdjacentSeasons helper method
    - Handle ALL_SEASON items (always appropriate)
    - Handle direct season match
    - Handle adjacent seasons (spring-summer, summer-autumn, autumn-winter, winter-spring)
    - Map seasons to temperature ranges in documentation
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [ ]* 10.2 Write property tests for seasonal appropriateness
    - **Property 29: Seasonal Appropriateness Filtering**
    - **Validates: Requirements 10.3**
  
  - [ ]* 10.3 Write unit tests for seasonal appropriateness
    - Test ALL_SEASON items
    - Test direct season matches
    - Test adjacent seasons
    - Test non-adjacent seasons
    - Test season wrap-around (winter-spring)
    - _Requirements: 10.1, 10.2, 10.3_

- [x] 11. Recommendation engine - Core generation algorithm
  - [x] 11.1 Implement recommendation generation algorithm
    - Create DTOs: RecommendationRequest, OutfitRecommendation
    - Implement generateRecommendations method
    - Fetch user's clothing items from repository
    - Apply filters (season, color preferences)
    - Group items by category (tops, bottoms, footwear, outerwear)
    - Sort items by wear count (prioritize less-worn items)
    - Generate outfit combinations with nested loops
    - Calculate color and fit compatibility scores
    - Skip low-scoring combinations (< 50.0)
    - Find compatible footwear and outerwear
    - Build recommendations with overall score calculation
    - Check seasonal appropriateness
    - Generate human-readable explanations
    - Sort by overall score descending
    - Limit results to requested amount (max 20)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 11.1, 11.2, 11.3, 11.4_
  
  - [x] 11.2 Implement RecommendationController
    - Create GET /api/recommendations endpoint
    - Add query parameters: season, occasion, colorPreference, limit
    - Extract userId from SecurityContext
    - Validate limit parameter (default 10, max 20)
    - Return empty list with 200 OK when no recommendations found
    - _Requirements: 11.1, 11.2, 11.3, 11.4_
  
  - [ ]* 11.3 Write property tests for recommendation engine
    - **Property 21: Wear Count Balancing**
    - **Validates: Requirements 7.5**
    - **Property 30: Recommendation Response Size Bounds**
    - **Validates: Requirements 11.3**
    - **Property 31: Recommendation Response Structure**
    - **Validates: Requirements 11.4**
    - **Property 32: Recommendation Filter Compliance**
    - **Validates: Requirements 11.2**
    - **Property 38: Recommendation Idempotence**
    - **Validates: Requirements 17.1**
    - **Property 39: Recommended Items Existence**
    - **Validates: Requirements 17.3**
    - **Property 41: Recommendation Storage Round-Trip**
    - **Validates: Requirements 17.5**
    - **Property 42: Graceful Handling of Invalid Data**
    - **Validates: Requirements 18.5**
  
  - [ ]* 11.4 Write unit tests for recommendation engine
    - Test empty closet returns empty list
    - Test filter application (season, color)
    - Test wear count prioritization
    - Test limit parameter enforcement
    - Test score calculation
    - Test explanation generation
    - _Requirements: 7.5, 11.1, 11.2, 11.3, 11.4_

- [x] 12. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Error handling and validation
  - [x] 13.1 Implement exception hierarchy
    - Create OutfitCreatorException base class with errorCode and httpStatus
    - Create ResourceNotFoundException (404)
    - Create ValidationException (400) with fieldErrors map
    - Create UnauthorizedException (401)
    - Create ForbiddenException (403)
    - Create InvalidFileTypeException (400)
    - Create FileSizeExceededException (413)
    - Create StorageException (500)
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_
  
  - [x] 13.2 Implement global exception handler
    - Create ErrorResponse DTO with timestamp, status, error, message, errorCode, fieldErrors, path
    - Create GlobalExceptionHandler with @RestControllerAdvice
    - Handle OutfitCreatorException and subclasses
    - Handle MethodArgumentNotValidException for validation errors
    - Handle generic Exception with safe error message (no internal details)
    - Log all errors appropriately
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_
  
  - [x] 13.3 Add validation annotations to all request DTOs
    - Add @NotBlank, @NotNull, @Size, @Past, @Email annotations
    - Add custom validation messages
    - Ensure all required fields are validated
    - _Requirements: 3.4, 3.5, 16.2, 16.3_
  
  - [ ]* 13.4 Write property tests for validation
    - **Property 36: Required Field Validation**
    - **Validates: Requirements 16.3**
    - **Property 37: Error Message Safety**
    - **Validates: Requirements 16.5**
  
  - [ ]* 13.5 Write unit tests for error handling
    - Test 404 for non-existent resources
    - Test 401 for invalid credentials
    - Test 403 for access denied
    - Test 400 for validation failures
    - Test 413 for oversized files
    - Test 500 for storage failures
    - Test error response format
    - Test error message safety (no sensitive info)
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

- [x] 14. Outfit compatibility score updates
  - [x] 14.1 Implement outfit score recalculation
    - Add method in OutfitService to recalculate compatibility scores
    - Trigger recalculation when clothing item attributes change
    - Update outfit's color and fit compatibility scores
    - Store scores in outfit or calculate on-demand
    - _Requirements: 17.4_
  
  - [ ]* 14.2 Write property test for score recalculation
    - **Property 40: Outfit Score Recalculation**
    - **Validates: Requirements 17.4**

- [ ] 15. API documentation with OpenAPI
  - [x] 15.1 Configure SpringDoc OpenAPI
    - Add SpringDoc OpenAPI dependency
    - Configure OpenAPI 3.0 documentation
    - Add API info (title, version, description)
    - Configure security scheme for JWT Bearer tokens
    - _Requirements: 14.1, 14.2_
  
  - [x] 15.2 Add OpenAPI annotations to controllers
    - Add @Operation annotations with summaries and descriptions
    - Add @ApiResponse annotations for all status codes
    - Add @Parameter annotations for query parameters
    - Add @Schema annotations to DTOs
    - Include example requests and responses
    - Document authentication requirements
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_
  
  - [x] 15.3 Verify API documentation accessibility
    - Ensure /swagger-ui.html is accessible
    - Ensure /v3/api-docs is accessible
    - Verify all endpoints are documented
    - Verify authentication scheme is documented
    - _Requirements: 14.1, 14.2, 14.3, 14.4_

- [ ] 16. Integration tests and end-to-end workflows
  - [ ]* 16.1 Write integration tests for authentication flow
    - Test complete registration and login flow
    - Test JWT token usage across multiple requests
    - Test profile update workflow
    - _Requirements: 1.1, 1.2, 1.4_
  
  - [ ]* 16.2 Write integration tests for clothing item workflow
    - Test create, retrieve, update, delete workflow
    - Test photo upload and retrieval
    - Test filtering and pagination
    - Test user isolation
    - _Requirements: 2.1, 3.1, 4.1, 4.2, 5.1, 5.2, 5.3, 5.5_
  
  - [ ]* 16.3 Write integration tests for outfit workflow
    - Test outfit creation with multiple items
    - Test outfit retrieval and update
    - Test outfit incompleteness when item deleted
    - _Requirements: 6.1, 6.2, 6.4, 6.5_
  
  - [ ]* 16.4 Write integration tests for recommendation workflow
    - Test end-to-end recommendation generation
    - Test with diverse closet (multiple colors, fits, seasons)
    - Test filter application
    - Test empty closet scenario
    - Verify color and fit compatibility in results
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.3, 9.1, 9.4, 10.3, 11.1, 11.2, 11.3_

- [ ] 17. Performance optimization and configuration
  - [x] 17.1 Add database indexes and query optimization
    - Verify indexes on foreign keys
    - Verify indexes on filter columns (category, season, color)
    - Add query hints if needed for complex queries
    - Test query performance with large datasets
    - _Requirements: 15.1, 15.2_
  
  - [x] 17.2 Configure connection pooling and caching
    - Configure HikariCP connection pool settings
    - Add caching for frequently accessed data (user profiles, color wheel)
    - Configure JPA fetch strategies
    - _Requirements: 15.1, 15.2, 15.4_
  
  - [x] 17.3 Add application configuration properties
    - Configure JWT secret and expiration
    - Configure file storage paths and limits
    - Configure pagination defaults
    - Configure CORS allowed origins
    - Configure database connection settings
    - Add separate profiles for dev, test, prod
    - _Requirements: 15.1, 15.2, 15.3, 15.4_

- [ ] 18. Custom generators for property-based tests
  - [x] 18.1 Create jqwik generators for domain objects
    - Create ClothingItemGenerator with valid attribute combinations
    - Create OutfitGenerator with valid item references
    - Create UserGenerator with valid email and password formats
    - Create ColorGenerator with valid color names
    - Create SeasonGenerator, FitCategoryGenerator, CategoryGenerator
    - Configure generators to produce realistic test data
    - _Requirements: All property tests_

- [ ] 19. Final checkpoint and verification
  - [x] 19.1 Run all tests and verify coverage
    - Run all unit tests
    - Run all property-based tests (100 iterations each)
    - Run all integration tests
    - Verify line coverage >= 80%
    - Verify branch coverage >= 75%
    - _Requirements: All_
  
  - [x] 19.2 Verify all API endpoints
    - Test all endpoints manually or with Postman/curl
    - Verify authentication flow
    - Verify photo upload and retrieval
    - Verify recommendation generation
    - Verify error responses
    - _Requirements: All_
  
  - [x] 19.3 Review and finalize documentation
    - Review OpenAPI documentation completeness
    - Review README with setup instructions
    - Document environment variables
    - Document API usage examples
    - _Requirements: 14.1, 14.2, 14.3, 14.4_

- [x] 20. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property-based tests use jqwik with 100 iterations per property
- All property tests include comment tags referencing design document properties
- Checkpoints ensure incremental validation and provide opportunities for user feedback
- Integration tests verify end-to-end workflows across multiple components
- The implementation follows Spring Boot best practices with layered architecture
- Security is enforced at every layer with JWT authentication and user-scoped data access
