# Implementation Plan: OutfitCreator Frontend

## Overview

This implementation plan breaks down the OutfitCreator frontend into discrete, incremental coding tasks. The frontend is a React + TypeScript application that integrates with the Spring Boot backend REST API. The implementation follows a bottom-up approach: shared components → API layer → custom hooks → feature components → pages → integration.

Each task builds on previous work, with checkpoints to validate progress. Testing tasks are marked as optional with `*` and can be skipped for faster MVP delivery.

## Tasks

- [x] 1. Project setup and configuration
  - Initialize Vite + React + TypeScript project
  - Configure Tailwind CSS for styling
  - Set up ESLint and Prettier for code quality
  - Configure environment variables (.env files)
  - Set up project directory structure (components, pages, hooks, api, types, utils)
  - Install core dependencies (react-router-dom, axios, zod, react-hook-form, react-dropzone)
  - Configure Vite proxy for local API development
  - _Requirements: 26, 27, 28, 30_

- [ ]* 1.1 Set up testing infrastructure
  - Configure Vitest and React Testing Library
  - Configure Playwright for E2E tests
  - Install fast-check for property-based testing
  - Create test utilities and mocks
  - _Requirements: 29_

- [x] 2. Define TypeScript types and validation schemas
  - [x] 2.1 Create core type definitions
    - Define User, ClothingItem, Outfit, OutfitItem types
    - Define enums (ClothingCategory, Season, FitCategory, ItemPosition)
    - Define OutfitRecommendation and related types
    - Define API response types (ApiResponse, PaginatedResponse, ErrorResponse)
    - Define form data types (LoginFormData, RegisterFormData, ClothingItemFormData, OutfitFormData)
    - Create types/index.ts as central export
    - _Requirements: 28_

  - [x] 2.2 Create Zod validation schemas
    - Define loginSchema for email and password validation
    - Define registerSchema with password strength requirements
    - Define clothingItemSchema with field constraints
    - Define outfitSchema with name and items validation
    - Define photoSchema for file type and size validation
    - Create schemas/index.ts as central export
    - _Requirements: 12, 28_


- [-] 3. Implement API service layer
  - [x] 3.1 Create base API client with Axios
    - Implement ApiClient class with baseURL configuration
    - Set up request interceptor to add JWT token to Authorization header
    - Set up response interceptor to handle 401 errors and redirect to login
    - Implement generic HTTP methods (get, post, put, delete, postFormData)
    - _Requirements: 1.2, 25_

  - [ ]* 3.2 Write property test for API client
    - **Property 2: Token Persistence**
    - **Validates: Requirements 1.2, 2.1, 2.2, 25**

  - [x] 3.3 Create AuthAPI service
    - Implement login(credentials) method
    - Implement register(data) method
    - Implement getProfile() method
    - Implement updateProfile(data) method
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 10.1, 10.2_

  - [x] 3.4 Create ClothingItemsAPI service
    - Implement getAll(filters, page, size) method with pagination
    - Implement getById(id) method
    - Implement create(data, photo) method with multipart form data
    - Implement update(id, data) method
    - Implement delete(id) method
    - Implement uploadPhoto(id, photo) method
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.6_

  - [x] 3.5 Create OutfitsAPI service
    - Implement getAll(page, size) method with pagination
    - Implement getById(id) method
    - Implement create(data) method
    - Implement update(id, data) method
    - Implement delete(id) method
    - _Requirements: 7.1, 7.5, 7.6, 7.7_

  - [x] 3.6 Create RecommendationsAPI service
    - Implement getRecommendations(filters) method
    - _Requirements: 9.1_

- [x] 4. Checkpoint - Verify API layer
  - Ensure all API services are properly typed
  - Verify interceptors are configured correctly
  - Ask the user if questions arise


- [x] 5. Implement shared UI components
  - [x] 5.1 Create Button component
    - Support variants (primary, secondary, danger, ghost)
    - Support sizes (sm, md, lg)
    - Handle loading state with spinner
    - Handle disabled state
    - Support fullWidth prop
    - _Requirements: 14.2_

  - [x] 5.2 Create Input component
    - Support label and error message display
    - Support various input types (text, email, password, etc.)
    - Handle required and disabled states
    - Apply proper accessibility attributes (aria-labels, aria-invalid)
    - _Requirements: 12.1, 12.2, 12.3, 16.7_

  - [x] 5.3 Create Select component
    - Support label and error message display
    - Render options from array
    - Handle required and disabled states
    - Apply proper accessibility attributes
    - _Requirements: 12.1, 12.2, 12.3, 16.7_

  - [x] 5.4 Create Modal component
    - Render modal overlay with backdrop
    - Support size variants (sm, md, lg, xl)
    - Implement focus trap when modal is open
    - Close on Escape key press
    - Close on backdrop click
    - Prevent background scrolling when open
    - Animate open/close transitions
    - Restore focus to trigger element on close
    - _Requirements: 16.2, 16.3, 20.1, 20.2, 20.3, 20.4, 20.5, 20.6_

  - [x] 5.5 Create LoadingSpinner component
    - Support size variants (sm, md, lg)
    - Support fullScreen mode
    - _Requirements: 14.1, 14.3_

  - [x] 5.6 Create ErrorMessage component
    - Display error message text
    - Provide optional retry button
    - Provide optional dismiss button
    - _Requirements: 13.1, 13.2, 13.7_

  - [x] 5.7 Create Pagination component
    - Display current page and total pages
    - Show page navigation buttons (previous, next, first, last)
    - Disable buttons appropriately at boundaries
    - Display total items count
    - _Requirements: 5.3, 5.4, 5.5_

  - [ ]* 5.8 Write unit tests for shared components
    - Test Button variants and states
    - Test Input validation display
    - Test Modal keyboard navigation
    - Test Pagination boundary conditions
    - _Requirements: 29_


- [x] 6. Implement custom hooks for state management
  - [x] 6.1 Create useAuth hook
    - Implement state for user, isAuthenticated, isLoading
    - Implement login(credentials) function that calls AuthAPI and stores token
    - Implement register(data) function that creates account and auto-logs in
    - Implement logout() function that removes token and clears user state
    - Implement updateProfile(data) function
    - Check for existing token on mount and restore session
    - _Requirements: 1.1, 1.2, 1.3, 1.7, 2.1, 2.2, 2.5, 10.2, 10.3, 18.1, 18.2, 18.3, 18.4, 18.5_

  - [ ]* 6.2 Write property test for useAuth hook
    - **Property 1: Login Round-Trip**
    - **Property 3: Logout Cleanup**
    - **Validates: Requirements 1.1, 1.2, 1.7, 2.1, 2.5, 18.5**

  - [x] 6.3 Create useClothingItems hook
    - Implement state for items, pagination, isLoading, error
    - Implement fetchItems(filters, page) function
    - Implement createItem(data, photo) function with optimistic update
    - Implement updateItem(id, data) function with optimistic update
    - Implement deleteItem(id) function with optimistic update
    - Implement uploadPhoto(id, photo) function
    - Handle errors and revert optimistic updates on failure
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.6, 5.1, 5.2, 21.1, 21.2, 21.3, 21.4_

  - [ ]* 6.4 Write property test for useClothingItems hook
    - **Property 5: Item Creation Round-Trip**
    - **Property 8: Item Update Preservation**
    - **Property 9: Item Deletion Removal**
    - **Validates: Requirements 3.1, 3.4, 3.5, 21.1, 21.2, 21.3**

  - [x] 6.5 Create useOutfits hook
    - Implement state for outfits, pagination, isLoading, error
    - Implement fetchOutfits(page) function
    - Implement createOutfit(data) function with optimistic update
    - Implement updateOutfit(id, data) function with optimistic update
    - Implement deleteOutfit(id) function with optimistic update
    - _Requirements: 7.5, 7.6, 7.7, 7.8, 21.1, 21.2, 21.3_

  - [ ]* 6.6 Write property test for useOutfits hook
    - **Property 16: Outfit Creation Round-Trip**
    - **Validates: Requirements 7.5, 7.8, 21.1**

  - [x] 6.7 Create useRecommendations hook
    - Implement state for recommendations, isLoading, error
    - Implement fetchRecommendations(filters) function
    - Implement saveRecommendation(recommendation, name) function that converts to outfit
    - _Requirements: 9.1, 9.8_

  - [x] 6.8 Create useAutoLogout hook
    - Implement inactivity timer (30 minutes default)
    - Reset timer on user interactions (mouse, keyboard, touch, scroll)
    - Call logout when timer expires
    - _Requirements: 2.3, 2.4_

- [~] 7. Checkpoint - Verify hooks layer
  - Test hooks with mock API responses
  - Verify state updates work correctly
  - Ensure all tests pass, ask the user if questions arise


- [~] 8. Implement utility functions and helpers
  - [x] 8.1 Create filter utility functions
    - Implement applyFilters(items, filters) function
    - Support category, season, color, and search query filters
    - Ensure filters work in combination
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ]* 8.2 Write property tests for filter functions
    - **Property 10: Category Filter Correctness**
    - **Property 11: Season Filter Correctness**
    - **Property 12: Search Filter Correctness**
    - **Property 13: Filter Combination**
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**

  - [x] 8.3 Create validation utility functions
    - Implement validateOutfit(outfit) function
    - Check name, items, position uniqueness, notes length
    - Return ValidationResult with isValid and errors array
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 8.5, 8.6_

  - [ ]* 8.4 Write property test for outfit validation
    - **Property 17: Outfit Validation**
    - **Property 18: Position Uniqueness**
    - **Validates: Requirements 7.1, 7.3, 7.4, 8.5, 8.6**

  - [x] 8.5 Create score display utility functions
    - Implement getScoreColor(score) function (green/yellow/orange/red thresholds)
    - Implement formatScore(score) function (percentage formatting)
    - Implement getScoreLabel(score) function (Excellent/Good/Fair/Poor)
    - _Requirements: 9.2, 9.3, 24.1, 24.2, 24.3, 24.4, 24.5, 24.6_

  - [ ]* 8.6 Write property test for score display functions
    - **Property 19: Score Display Consistency**
    - **Validates: Requirements 9.2, 9.3, 24.1, 24.2, 24.3, 24.4**

  - [x] 8.7 Create photo upload utility functions
    - Implement handlePhotoUpload(file) function with validation
    - Validate file type (JPEG, PNG, GIF only)
    - Validate file size (5MB max)
    - Create preview URL with URL.createObjectURL
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [ ]* 8.8 Write property test for photo validation
    - **Property 6: Photo Upload Validation**
    - **Property 7: File Size Validation**
    - **Validates: Requirements 4.1, 4.2, 4.3**

  - [x] 8.9 Create debounce utility function
    - Implement useDebounce(value, delay) hook
    - Use for search input (300ms delay)
    - _Requirements: 6.7, 17.3_


- [~] 9. Implement feature components
  - [x] 9.1 Create ClothingItemCard component
    - Display item photo with fallback placeholder
    - Show item name, brand, category
    - Display primary and secondary color indicators
    - Provide edit and delete action buttons
    - Support selectable mode with selected state
    - Handle onClick for selection
    - _Requirements: 5.2, 19.1, 19.2, 19.3, 22.1, 22.2, 22.3, 22.4_

  - [ ]* 9.2 Write unit tests for ClothingItemCard
    - Test rendering with and without photo
    - Test action button callbacks
    - Test selectable mode
    - _Requirements: 29_

  - [x] 9.3 Create ClothingItemForm component
    - Render form fields (name, brand, colors, category, size, season, fit, purchase date)
    - Integrate react-hook-form with Zod validation
    - Support photo upload with react-dropzone
    - Display photo preview
    - Show field-level validation errors
    - Handle form submission with loading state
    - Support edit mode with pre-filled data
    - _Requirements: 3.2, 3.3, 3.6, 3.7, 3.8, 4.4, 4.5, 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 9.4 Write unit tests for ClothingItemForm
    - Test validation error display
    - Test photo upload and preview
    - Test form submission
    - _Requirements: 29_

  - [x] 9.5 Create FilterPanel component
    - Provide select inputs for category and season filters
    - Provide text input for color filter
    - Provide search input for text query
    - Display active filter count badge
    - Provide reset button to clear all filters
    - Call onFilterChange callback when filters change
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.6, 6.8_

  - [x] 9.6 Create OutfitCard component
    - Display outfit name and notes
    - Show creation date
    - Display grid of item photos
    - Show completeness indicator (complete/incomplete)
    - Provide edit and delete action buttons
    - Handle onClick for viewing details
    - _Requirements: 7.8, 23.1, 23.2, 23.3_

  - [x] 9.7 Create OutfitBuilder component
    - Render as modal dialog
    - Display input for outfit name and notes
    - Show available clothing items grouped by category
    - Organize selected items by position slots (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORY)
    - Support drag-and-drop item selection with react-dropzone
    - Replace existing item when adding to same position
    - Validate outfit before saving
    - Handle save and cancel actions
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

  - [ ]* 9.8 Write unit tests for OutfitBuilder
    - Test item selection and position assignment
    - Test validation on save
    - Test modal close behavior
    - _Requirements: 29_

  - [x] 9.9 Create RecommendationCard component
    - Display recommended items with photos
    - Show color compatibility score with color-coded indicator
    - Show fit compatibility score with color-coded indicator
    - Display overall score
    - Show seasonal appropriateness warning if applicable
    - Display explanation text
    - Provide "Save as Outfit" button
    - _Requirements: 9.2, 9.3, 9.4, 9.5, 9.6, 9.9_

  - [ ]* 9.10 Write unit tests for RecommendationCard
    - Test score color coding
    - Test seasonal warning display
    - Test save button callback
    - _Requirements: 29_

- [x] 10. Checkpoint - Verify feature components
  - Test components in isolation with Storybook or manual testing
  - Verify all props and callbacks work correctly
  - Ensure all tests pass, ask the user if questions arise


- [x] 11. Implement layout and navigation components
  - [x] 11.1 Create Navigation component
    - Display app logo and title
    - Show navigation links (Closet, Outfits, Recommendations, Profile)
    - Highlight active route using react-router-dom
    - Display current user's name
    - Provide logout button
    - Make responsive for mobile (hamburger menu)
    - _Requirements: 11.1, 11.2, 11.3, 11.6, 11.7, 15.1, 15.2, 15.3_

  - [x] 11.2 Create MainLayout component
    - Render Navigation at top
    - Render page content in main area
    - Apply responsive container styling
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_

  - [x] 11.3 Create ErrorBoundary component
    - Catch React errors in component tree
    - Display fallback error page
    - Provide refresh button
    - Log errors to console
    - _Requirements: 13.7_

  - [x] 11.4 Create ProtectedRoute component
    - Check if user is authenticated
    - Redirect to login page if not authenticated
    - Render children if authenticated
    - _Requirements: 11.4_


- [x] 12. Implement authentication pages
  - [x] 12.1 Create LoginPage component
    - Render login form with email and password inputs
    - Integrate react-hook-form with loginSchema validation
    - Display validation errors
    - Call useAuth login function on submit
    - Show loading state during authentication
    - Display authentication errors
    - Redirect to /closet on successful login
    - Provide link to registration page
    - _Requirements: 1.1, 1.2, 1.3, 11.5, 12.1, 12.2, 12.4, 12.5, 12.6, 14.2_

  - [ ]* 12.2 Write integration test for LoginPage
    - Test successful login flow
    - Test validation error display
    - Test authentication error handling
    - _Requirements: 29_

  - [x] 12.3 Create RegisterPage component
    - Render registration form (email, password, confirmPassword, firstName, lastName)
    - Integrate react-hook-form with registerSchema validation
    - Validate password strength (8+ chars, uppercase, lowercase, number)
    - Validate password confirmation match
    - Display validation errors
    - Call useAuth register function on submit
    - Show loading state during registration
    - Redirect to /closet on successful registration (auto-login)
    - _Requirements: 1.4, 1.5, 1.6, 12.1, 12.2, 12.4, 12.5, 12.6, 14.2_

  - [ ]* 12.4 Write integration test for RegisterPage
    - Test successful registration flow
    - Test password validation
    - Test password mismatch error
    - _Requirements: 29_


- [x] 13. Implement ClosetPage
  - [x] 13.1 Create ClosetPage component
    - Use useClothingItems hook to fetch and manage items
    - Render FilterPanel for filtering items
    - Display "Add Item" button to open creation modal
    - Render grid of ClothingItemCard components
    - Show LoadingSpinner while fetching
    - Display ErrorMessage on fetch failure with retry
    - Render Pagination component
    - Handle filter changes and refetch items
    - Handle page changes and refetch items
    - Open ClothingItemForm modal for creation
    - Handle item deletion with confirmation
    - _Requirements: 3.1, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3, 6.4, 6.5, 13.2, 14.1, 14.3_

  - [x] 13.2 Implement lazy loading for images
    - Create LazyImage component using Intersection Observer
    - Use in ClothingItemCard for photo display
    - _Requirements: 17.1_

  - [x] 13.3 Add edit functionality to ClosetPage
    - Open ClothingItemForm modal in edit mode
    - Pre-fill form with existing item data
    - Handle item update
    - _Requirements: 3.4_

  - [ ]* 13.4 Write integration test for ClosetPage
    - Test item fetching and display
    - Test filtering functionality
    - Test pagination
    - Test item creation flow
    - Test item deletion flow
    - _Requirements: 29_

  - [ ]* 13.5 Write property test for pagination
    - **Property 14: Page Size Limit**
    - **Property 15: Page Navigation**
    - **Validates: Requirements 5.3, 5.4, 5.5**


- [x] 14. Implement OutfitsPage
  - [x] 14.1 Create OutfitsPage component
    - Use useOutfits hook to fetch and manage outfits
    - Display "Create Outfit" button to open OutfitBuilder
    - Render grid of OutfitCard components
    - Show LoadingSpinner while fetching
    - Display ErrorMessage on fetch failure with retry
    - Render Pagination component
    - Handle page changes and refetch outfits
    - Open OutfitBuilder modal for creation
    - Handle outfit deletion with confirmation
    - _Requirements: 7.5, 7.6, 7.7, 7.8, 8.1, 13.2, 14.1, 14.3_

  - [x] 14.2 Add edit functionality to OutfitsPage
    - Open OutfitBuilder modal in edit mode
    - Pre-fill builder with existing outfit data
    - Handle outfit update
    - _Requirements: 7.6_

  - [ ]* 14.3 Write integration test for OutfitsPage
    - Test outfit fetching and display
    - Test outfit creation flow
    - Test outfit deletion flow
    - Test outfit editing flow
    - _Requirements: 29_


- [x] 15. Implement RecommendationsPage
  - [x] 15.1 Create RecommendationsPage component
    - Use useRecommendations hook to fetch recommendations
    - Render filter controls (season select, color preference input, limit)
    - Display grid of RecommendationCard components
    - Show LoadingSpinner while fetching
    - Display ErrorMessage on fetch failure with retry
    - Handle filter changes and refetch recommendations
    - Handle save recommendation as outfit
    - Show success message after saving
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9, 13.2, 14.1, 14.3_

  - [ ]* 15.2 Write property test for recommendation filters
    - **Property 21: Filter Application**
    - **Validates: Requirements 9.6, 9.7**

  - [ ]* 15.3 Write integration test for RecommendationsPage
    - Test recommendation fetching and display
    - Test filter application
    - Test save as outfit flow
    - _Requirements: 29_

  - [ ]* 15.4 Write property test for recommendation save conversion
    - **Property 20: Recommendation Save Conversion**
    - **Validates: Requirements 9.8**


- [-] 16. Implement ProfilePage
  - [x] 16.1 Create ProfilePage component
    - Use useAuth hook to access user data
    - Display user information (firstName, lastName, email)
    - Show account statistics (total items, total outfits)
    - Render profile update form
    - Integrate react-hook-form for profile updates
    - Call useAuth updateProfile function on submit
    - Show loading state during update
    - Display success message after update
    - Provide logout button
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 12.1, 12.2, 14.2_

  - [ ]* 16.2 Write integration test for ProfilePage
    - Test profile display
    - Test profile update flow
    - Test logout functionality
    - _Requirements: 29_


- [x] 17. Implement routing and app integration
  - [x] 17.1 Create App component with routing
    - Set up BrowserRouter from react-router-dom
    - Wrap app in ErrorBoundary
    - Define routes for all pages (/, /login, /register, /closet, /outfits, /recommendations, /profile)
    - Use ProtectedRoute for authenticated pages
    - Redirect / to /closet if authenticated, /login if not
    - Apply MainLayout to authenticated routes
    - _Requirements: 11.1, 11.2, 11.4, 11.5_

  - [x] 17.2 Create AuthContext provider
    - Wrap App with AuthContext to provide useAuth hook globally
    - Initialize auth state on app mount
    - _Requirements: 1.1, 1.2, 2.5_

  - [x] 17.3 Integrate useAutoLogout hook
    - Call useAutoLogout in App component for authenticated users
    - _Requirements: 2.3, 2.4_

  - [x] 17.4 Implement code splitting for routes
    - Use React.lazy and Suspense for page components
    - Show LoadingSpinner as fallback
    - _Requirements: 17.2, 27.2, 27.3_

  - [ ]* 17.5 Write E2E test for complete user flow
    - Test registration → login → create item → create outfit → view recommendations
    - Use Playwright
    - _Requirements: 29_

- [x] 18. Checkpoint - Verify complete application
  - Test all pages and navigation
  - Verify authentication flow works end-to-end
  - Test all CRUD operations
  - Ensure all tests pass, ask the user if questions arise


- [x] 19. Implement responsive design and accessibility
  - [x] 19.1 Add responsive breakpoints with Tailwind
    - Configure mobile (<768px), tablet (768px-1024px), desktop (>1024px) breakpoints
    - Apply responsive grid layouts (1 column mobile, 2 columns tablet, 3+ columns desktop)
    - Make navigation responsive with hamburger menu on mobile
    - Ensure touch targets are appropriately sized (min 44x44px)
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6_

  - [x] 19.2 Implement keyboard navigation
    - Ensure all interactive elements are keyboard accessible
    - Add proper tab order
    - Implement focus visible styles
    - Test modal focus trap
    - Test Escape key to close modals
    - _Requirements: 16.1, 16.2, 16.3_

  - [x] 19.3 Add ARIA attributes and labels
    - Add aria-label to buttons without text
    - Add aria-invalid to form fields with errors
    - Add aria-live regions for dynamic content updates
    - Add alt text to all images
    - Associate labels with form inputs
    - _Requirements: 16.4, 16.5, 16.6, 16.7_

  - [x] 19.4 Verify color contrast
    - Ensure text meets WCAG 2.1 AA contrast ratios (4.5:1 for normal text, 3:1 for large text)
    - Test with browser accessibility tools
    - _Requirements: 16.5_

  - [ ]* 19.5 Write accessibility tests
    - Test keyboard navigation with automated tools
    - Test screen reader compatibility manually
    - _Requirements: 29_


- [x] 20. Implement performance optimizations
  - [x] 20.1 Add request debouncing
    - Apply useDebounce to search input (300ms delay)
    - Apply useDebounce to filter inputs
    - _Requirements: 6.7, 17.3_

  - [x] 20.2 Implement React.memo for expensive components
    - Memoize ClothingItemCard
    - Memoize OutfitCard
    - Memoize RecommendationCard
    - _Requirements: 17.6_

  - [x] 20.3 Optimize bundle size
    - Configure Vite code splitting by route
    - Create separate chunks for vendor libraries (react, axios, form libraries)
    - Verify bundle size with build analysis
    - _Requirements: 17.2, 27.2, 27.3_

  - [x] 20.4 Add image optimization
    - Implement lazy loading with Intersection Observer
    - Use thumbnail URLs for list views if backend provides them
    - _Requirements: 17.1, 19.4_


- [x] 21. Implement error handling improvements
  - [x] 21.1 Add specific error handling for API errors
    - Handle network errors with user-friendly messages
    - Handle 404 errors with "not found" messages and navigation
    - Handle 500 errors with generic messages
    - Handle validation errors with field-level messages
    - Provide retry buttons for recoverable errors
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6_

  - [x] 21.2 Add success notifications
    - Show success message after item creation
    - Show success message after outfit creation
    - Show success message after profile update
    - Auto-dismiss notifications after 3 seconds
    - _Requirements: 13.6_

  - [x] 21.3 Improve loading states
    - Disable submit buttons during form submission
    - Show loading spinners in buttons
    - Prevent duplicate submissions
    - _Requirements: 14.2, 14.4, 14.5_

  - [ ]* 21.4 Write tests for error scenarios
    - Test network error handling
    - Test 401 redirect behavior
    - Test validation error display
    - _Requirements: 29_


- [x] 22. Security hardening
  - [x] 22.1 Implement TokenManager utility
    - Create TokenManager class for secure token handling
    - Validate token format before storing
    - Never log tokens
    - _Requirements: 25_

  - [x] 22.2 Add input sanitization
    - Sanitize user-generated content before display
    - Escape HTML in notes and text fields
    - _Requirements: 12.1, 12.2_

  - [x] 22.3 Configure Content Security Policy
    - Add CSP meta tag to index.html
    - Restrict script sources
    - _Requirements: 26_

  - [x] 22.4 Verify HTTPS enforcement
    - Ensure API calls use HTTPS in production
    - Configure environment variables for production API URL
    - _Requirements: 26_


- [x] 23. Production build and deployment preparation
  - [x] 23.1 Configure production environment variables
    - Create .env.production file with API base URL
    - Set max file size and supported image types
    - _Requirements: 26_

  - [x] 23.2 Optimize production build
    - Configure Vite for production minification
    - Enable source map generation for debugging (optional)
    - Configure manual chunks for vendor libraries
    - _Requirements: 27.1, 27.2, 27.3, 27.4_

  - [x] 23.3 Test production build locally
    - Run `npm run build`
    - Run `npm run preview` to test production build
    - Verify all features work in production mode
    - Check bundle sizes
    - _Requirements: 27.4_

  - [x] 23.4 Create deployment documentation
    - Document environment variables needed
    - Document build and deployment steps
    - Document CORS configuration requirements for backend
    - Document browser requirements
    - _Requirements: 26, 27_

- [x] 24. Final checkpoint - Production readiness
  - Run all tests (unit, integration, property-based, E2E)
  - Verify production build works correctly
  - Test on multiple browsers (Chrome, Firefox, Safari, Edge)
  - Test on multiple devices (mobile, tablet, desktop)
  - Verify accessibility with automated tools
  - Ensure all tests pass, ask the user if questions arise


## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation and provide opportunities to ask questions
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Integration tests validate component interactions and API integration
- E2E tests validate complete user workflows
- The implementation follows a bottom-up approach: utilities → API → hooks → components → pages → integration
- All code should use TypeScript strict mode for type safety
- All components should be accessible (WCAG 2.1 AA)
- All API calls should include proper error handling
- All forms should include validation with user-friendly error messages

## Property-Based Test Reference

The following properties from the design document should be tested:

1. **Property 1: Login Round-Trip** - Task 6.2
2. **Property 2: Token Persistence** - Task 3.2
3. **Property 3: Logout Cleanup** - Task 6.2
4. **Property 5: Item Creation Round-Trip** - Task 6.4
5. **Property 6: Photo Upload Validation** - Task 8.8
6. **Property 7: File Size Validation** - Task 8.8
7. **Property 8: Item Update Preservation** - Task 6.4
8. **Property 9: Item Deletion Removal** - Task 6.4
9. **Property 10: Category Filter Correctness** - Task 8.2
10. **Property 11: Season Filter Correctness** - Task 8.2
11. **Property 12: Search Filter Correctness** - Task 8.2
12. **Property 13: Filter Combination** - Task 8.2
13. **Property 14: Page Size Limit** - Task 13.5
14. **Property 15: Page Navigation** - Task 13.5
15. **Property 16: Outfit Creation Round-Trip** - Task 6.6
16. **Property 17: Outfit Validation** - Task 8.4
17. **Property 18: Position Uniqueness** - Task 8.4
18. **Property 19: Score Display Consistency** - Task 8.6
19. **Property 20: Recommendation Save Conversion** - Task 15.4
20. **Property 21: Filter Application** - Task 15.2

