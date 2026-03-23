package unit.infrastructure.security;

import com.example.outfitcreator.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET =
            "test-secret-key-for-jwt-util-unit-tests-must-be-at-least-256-bits-long!!";
    private static final long EXPIRATION_MS = 86400000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate a non-null, non-blank token")
        void shouldGenerateNonNullToken() {
            String token = jwtUtil.generateToken(1L, "user@example.com");

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Should generate a well-formed JWT with three parts")
        void shouldGenerateWellFormedToken() {
            String token = jwtUtil.generateToken(1L, "user@example.com");

            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("extractUserId")
    class ExtractUserIdTests {

        @Test
        @DisplayName("Should extract the correct userId from a generated token")
        void shouldExtractCorrectUserId() {
            String token = jwtUtil.generateToken(42L, "user@example.com");

            assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("extractEmail")
    class ExtractEmailTests {

        @Test
        @DisplayName("Should extract the correct email from a generated token")
        void shouldExtractCorrectEmail() {
            String token = jwtUtil.generateToken(1L, "test@example.com");

            assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for a freshly generated valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtUtil.generateToken(1L, "user@example.com");

            assertThat(jwtUtil.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Should return false for a token signed with a different secret")
        void shouldReturnFalseForTokenWithDifferentSecret() {
            JwtUtil otherJwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(otherJwtUtil, "jwtSecret",
                    "completely-different-secret-key-also-at-least-256-bits-long-for-testing!!");
            ReflectionTestUtils.setField(otherJwtUtil, "jwtExpirationMs", EXPIRATION_MS);

            String tamperedToken = otherJwtUtil.generateToken(1L, "user@example.com");

            assertThat(jwtUtil.validateToken(tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("Should return false for a garbage string")
        void shouldReturnFalseForGarbageString() {
            assertThat(jwtUtil.validateToken("this.is.not.a.jwt")).isFalse();
        }

        @Test
        @DisplayName("Should return false for an already expired token")
        void shouldReturnFalseForExpiredToken() {
            JwtUtil expiredJwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(expiredJwtUtil, "jwtSecret", TEST_SECRET);
            ReflectionTestUtils.setField(expiredJwtUtil, "jwtExpirationMs", -1000L);

            String expiredToken = expiredJwtUtil.generateToken(1L, "user@example.com");

            assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
        }
    }
}
