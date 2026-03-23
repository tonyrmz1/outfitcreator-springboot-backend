package unit.infrastructure.security;

import com.example.outfitcreator.feature.auth.repository.UserRepository;
import com.example.outfitcreator.infrastructure.security.JwtAuthenticationFilter;
import com.example.outfitcreator.infrastructure.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    // doFilterInternal is protected; we invoke the public doFilter which delegates to it internally

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void resetContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("When no valid Authorization header is present")
    class NoAuthHeaderTests {

        @Test
        @DisplayName("Should pass through and not set authentication when header is absent")
        void shouldPassThroughWhenHeaderAbsent() throws Exception {
            when(request.getHeader("Authorization")).thenReturn(null);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter((ServletRequest) request, (ServletResponse) response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should pass through when Authorization header is not a Bearer token")
        void shouldPassThroughForNonBearerHeader() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter((ServletRequest) request, (ServletResponse) response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("When a Bearer token is present")
    class BearerTokenTests {

        @Test
        @DisplayName("Should pass through without auth when token fails validation")
        void shouldPassThroughWhenTokenIsInvalid() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.value");
            when(jwtUtil.validateToken("invalid.token.value")).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter((ServletRequest) request, (ServletResponse) response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should pass through without auth when user no longer exists in the database")
        void shouldPassThroughWhenUserDeletedFromDb() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
            when(jwtUtil.validateToken("valid.token.here")).thenReturn(true);
            when(jwtUtil.extractUserId("valid.token.here")).thenReturn(1L);
            when(userRepository.existsById(1L)).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter((ServletRequest) request, (ServletResponse) response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should set authentication in SecurityContext when token is valid and user exists")
        void shouldSetAuthenticationForValidTokenAndExistingUser() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
            when(jwtUtil.validateToken("valid.token.here")).thenReturn(true);
            when(jwtUtil.extractUserId("valid.token.here")).thenReturn(42L);
            when(userRepository.existsById(42L)).thenReturn(true);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter((ServletRequest) request, (ServletResponse) response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .isEqualTo(42L);
        }
    }
}
