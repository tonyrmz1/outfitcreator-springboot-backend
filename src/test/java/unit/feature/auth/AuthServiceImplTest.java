package unit.feature.auth;

import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.feature.auth.dto.request.LoginRequest;
import com.example.outfitcreator.feature.auth.dto.request.RegisterRequest;
import com.example.outfitcreator.feature.auth.dto.request.UpdateProfileRequest;
import com.example.outfitcreator.feature.auth.dto.response.LoginResponse;
import com.example.outfitcreator.feature.auth.dto.response.UserDTO;
import com.example.outfitcreator.feature.auth.repository.UserRepository;
import com.example.outfitcreator.feature.auth.service.AuthServiceImpl;
import com.example.outfitcreator.infrastructure.security.JwtUtil;
import com.example.outfitcreator.shared.exception.ResourceNotFoundException;
import com.example.outfitcreator.shared.exception.UnauthorizedException;
import com.example.outfitcreator.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("$2a$encoded_password")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user and return a populated UserDTO")
        void shouldRegisterNewUser() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("new@example.com");
            request.setPassword("password123");
            request.setFirstName("Jane");
            request.setLastName("Smith");

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });

            UserDTO result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("new@example.com");
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when email is already in use")
        void shouldThrowValidationExceptionWhenEmailAlreadyInUse() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");
            request.setFirstName("John");
            request.setLastName("Doe");

            when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Email already in use");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("Should return a LoginResponse with token on valid credentials")
        void shouldReturnTokenOnValidCredentials() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateToken(1L, "user@example.com")).thenReturn("jwt.token.value");

            LoginResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt.token.value");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not found")
        void shouldThrowWhenUserNotFound() {
            LoginRequest request = new LoginRequest();
            request.setEmail("unknown@example.com");
            request.setPassword("password123");

            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when password does not match")
        void shouldThrowWhenPasswordDoesNotMatch() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongPassword");

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(jwtUtil, never()).generateToken(any(), any());
        }
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfileTests {

        @Test
        @DisplayName("Should return UserDTO for an existing user")
        void shouldReturnUserDTOForExistingUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDTO result = authService.getProfile(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("user@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getProfile(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update first/last name and return updated UserDTO")
        void shouldUpdateProfile() {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDTO result = authService.updateProfile(1L, request);

            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.updateProfile(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository, never()).save(any());
        }
    }
}
