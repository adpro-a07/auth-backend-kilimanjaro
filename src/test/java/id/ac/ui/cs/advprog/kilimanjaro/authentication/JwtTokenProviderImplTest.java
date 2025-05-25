package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtTokenProviderImplTest {

    @Mock
    private SigningKeyProvider keyProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private BaseUser mockUser;

    private Clock fixedClock;
    private JwtTokenProviderImpl jwtTokenProvider;
    private final SecretKey testKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final String testUsername = "test@example.com";
    private final UUID testUserId = UUID.randomUUID();
    private final Instant fixedInstant = Instant.parse("2023-01-01T12:00:00Z");
    private final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private final long REFRESH_TOKEN_EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        when(keyProvider.getKey()).thenReturn(testKey);
        when(jwtProperties.getAccessExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);
        when(jwtProperties.getRefreshExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);

        jwtTokenProvider = new JwtTokenProviderImpl(
                keyProvider,
                userRepository,
                jwtProperties,
                tokenBlacklist,
                fixedClock
        );
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should throw NullPointerException when keyProvider is null")
        void shouldThrowExceptionWhenKeyProviderIsNull() {
            assertThrows(NullPointerException.class, () -> new JwtTokenProviderImpl(
                    null, userRepository, jwtProperties, tokenBlacklist, fixedClock
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when userRepository is null")
        void shouldThrowExceptionWhenUserRepositoryIsNull() {
            assertThrows(NullPointerException.class, () -> new JwtTokenProviderImpl(
                    keyProvider, null, jwtProperties, tokenBlacklist, fixedClock
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when jwtProperties is null")
        void shouldThrowExceptionWhenJwtPropertiesIsNull() {
            assertThrows(NullPointerException.class, () -> new JwtTokenProviderImpl(
                    keyProvider, userRepository, null, tokenBlacklist, fixedClock
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when tokenBlacklist is null")
        void shouldThrowExceptionWhenTokenBlacklistIsNull() {
            assertThrows(NullPointerException.class, () -> new JwtTokenProviderImpl(
                    keyProvider, userRepository, jwtProperties, null, fixedClock
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when clock is null")
        void shouldThrowExceptionWhenClockIsNull() {
            assertThrows(NullPointerException.class, () -> new JwtTokenProviderImpl(
                    keyProvider, userRepository, jwtProperties, tokenBlacklist, null
            ));
        }
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        private Map<String, Object> claims;

        @BeforeEach
        void setUp() {
            claims = new HashMap<>();
            claims.put("userId", testUserId.toString());
        }

        @Test
        @DisplayName("Should generate access token with correct claims")
        void shouldGenerateAccessToken() {
            String token = jwtTokenProvider.generateAccessToken(testUsername, claims);

            assertNotNull(token);

            Claims extractedClaims = Jwts.parserBuilder()
                    .setClock(() -> Date.from(fixedInstant))
                    .setSigningKey(testKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            assertEquals(testUsername, extractedClaims.getSubject());
            assertEquals(testUserId.toString(), extractedClaims.get("userId"));
            assertEquals("access", extractedClaims.get("type"));

            Date expectedExpiration = Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION));
            assertEquals(expectedExpiration, extractedClaims.getExpiration());
        }

        @Test
        @DisplayName("Should generate refresh token with correct claims")
        void shouldGenerateRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken(testUsername, claims);

            assertNotNull(token);

            Claims extractedClaims = Jwts.parserBuilder()
                    .setClock(() -> Date.from(fixedInstant))
                    .setSigningKey(testKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            assertEquals(testUsername, extractedClaims.getSubject());
            assertEquals(testUserId.toString(), extractedClaims.get("userId"));
            assertEquals("refresh", extractedClaims.get("type"));

            Date expectedExpiration = Date.from(fixedInstant.plusMillis(REFRESH_TOKEN_EXPIRATION));
            assertEquals(expectedExpiration, extractedClaims.getExpiration());
        }

        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.generateAccessToken(null, claims));

            assertTrue(exception.getMessage().contains("Username must not be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception when username is empty")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.generateAccessToken("", claims));

            assertTrue(exception.getMessage().contains("Username must not be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception when claims are null")
        void shouldThrowExceptionWhenClaimsAreNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.generateAccessToken(testUsername, null));

            assertTrue(exception.getMessage().contains("Additional claims must not be null"));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        private String validToken;
        private Map<String, Object> claims;

        @BeforeEach
        void setUp() {
            claims = new HashMap<>();
            claims.put("userId", testUserId.toString());
            claims.put("type", "access");

            validToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant))
                    .setExpiration(Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION)))
                    .addClaims(claims)
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
            when(tokenBlacklist.isBlacklisted(validToken)).thenReturn(false);
        }

        @Test
        @DisplayName("Should validate valid token")
        void shouldValidateValidToken() {
            when(userRepository.existsById(testUserId)).thenReturn(true);
            boolean result = jwtTokenProvider.validateToken(validToken);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should not validate token if blacklisted")
        void shouldNotValidateBlacklistedToken() {
            when(tokenBlacklist.isBlacklisted(validToken)).thenReturn(true);

            boolean result = jwtTokenProvider.validateToken(validToken);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should not validate token if expired")
        void shouldNotValidateExpiredToken() {
            String expiredToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant.minusSeconds(7200)))
                    .setExpiration(Date.from(fixedInstant.minusSeconds(3600)))
                    .addClaims(claims)
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            boolean result = jwtTokenProvider.validateToken(expiredToken);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should not validate token if user not found")
        void shouldNotValidateTokenIfUserNotFound() {
            when(userRepository.existsById(testUserId)).thenReturn(false);

            boolean result = jwtTokenProvider.validateToken(validToken);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should not validate token with invalid signature")
        void shouldNotValidateTokenWithInvalidSignature() {
            SecretKey differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            String invalidToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant))
                    .setExpiration(Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION)))
                    .addClaims(claims)
                    .signWith(differentKey, SignatureAlgorithm.HS256)
                    .compact();

            boolean result = jwtTokenProvider.validateToken(invalidToken);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should not validate token with invalid userId format")
        void shouldNotValidateTokenWithInvalidUserIdFormat() {
            Map<String, Object> invalidClaims = new HashMap<>();
            invalidClaims.put("userId", "not-a-uuid");
            invalidClaims.put("type", "access");

            String invalidToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant))
                    .setExpiration(Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION)))
                    .addClaims(invalidClaims)
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            boolean result = jwtTokenProvider.validateToken(invalidToken);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw exception when token is null")
        void shouldThrowExceptionWhenTokenIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.validateToken(null));

            assertTrue(exception.getMessage().contains("Token must not be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception when token is empty")
        void shouldThrowExceptionWhenTokenIsEmpty() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.validateToken(""));

            assertTrue(exception.getMessage().contains("Token must not be null or empty"));
        }
    }

    @Nested
    @DisplayName("Token Information Extraction Tests")
    class TokenInformationExtractionTests {

        private String validToken;

        @BeforeEach
        void setUp() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", testUserId.toString());
            claims.put("type", "access");
            claims.put("customClaim", "customValue");

            validToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant))
                    .setExpiration(Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION)))
                    .addClaims(claims)
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
            when(tokenBlacklist.isBlacklisted(validToken)).thenReturn(false);
        }

        @Test
        @DisplayName("Should extract email from token")
        void shouldExtractEmailFromToken() {
            assertEquals(testUsername, jwtTokenProvider.getEmailFromToken(validToken));
        }

        @Test
        @DisplayName("Should extract userId from token")
        void shouldExtractUserIdFromToken() {
            assertEquals(testUserId, jwtTokenProvider.getUserIdFromToken(validToken));
        }

        @Test
        @DisplayName("Should throw exception when userId format is invalid")
        void shouldThrowExceptionWhenUserIdFormatIsInvalid() {
            Map<String, Object> invalidClaims = new HashMap<>();
            invalidClaims.put("userId", "not-a-uuid");
            invalidClaims.put("type", "access");

            String invalidToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant))
                    .setExpiration(Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION)))
                    .addClaims(invalidClaims)
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            when(userRepository.findById((UUID) any())).thenReturn(Optional.of(mockUser));
            when(tokenBlacklist.isBlacklisted(invalidToken)).thenReturn(false);

            assertThrows(AuthenticationException.class, () -> jwtTokenProvider.getUserIdFromToken(invalidToken));
        }

        @Test
        @DisplayName("Should extract token type")
        void shouldExtractTokenType() {
            assertEquals("access", jwtTokenProvider.getTokenType(validToken));
        }

        @Test
        @DisplayName("Should extract custom claim by name and type")
        void shouldExtractCustomClaimByNameAndType() {
            assertEquals("customValue", jwtTokenProvider.getClaimFromToken(validToken, "customClaim", String.class));
        }

        @Test
        @DisplayName("Should extract claim using function")
        void shouldExtractClaimUsingFunction() {
            Function<Claims, String> claimsResolver = claims -> claims.get("customClaim", String.class);
            assertEquals("customValue", jwtTokenProvider.getClaimFromToken(validToken, claimsResolver));
        }

        @Test
        @DisplayName("Should get expiration date from token")
        void shouldGetExpirationDateFromToken() {
            Date expectedExpiration = Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION));
            assertEquals(expectedExpiration, jwtTokenProvider.getExpirationDateFromToken(validToken));
        }
    }

    @Nested
    @DisplayName("Token Invalidation Tests")
    class TokenInvalidationTests {

        private String validToken;
        private Date expirationDate;

        @BeforeEach
        void setUp() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", testUserId.toString());
            claims.put("type", "access");

            expirationDate = Date.from(fixedInstant.plusMillis(ACCESS_TOKEN_EXPIRATION));

            validToken = Jwts.builder()
                    .setSubject(testUsername)
                    .setIssuedAt(Date.from(fixedInstant))
                    .setExpiration(expirationDate)
                    .addClaims(claims)
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
            when(tokenBlacklist.isBlacklisted(validToken)).thenReturn(false);
        }

        @Test
        @DisplayName("Should invalidate token")
        void shouldInvalidateToken() {
            jwtTokenProvider.invalidateToken(validToken);

            verify(tokenBlacklist).blacklist(eq(validToken), eq(expirationDate.toInstant().toEpochMilli()));
        }

        @Test
        @DisplayName("Should throw exception when token is null")
        void shouldThrowExceptionWhenTokenIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.invalidateToken(null));

            assertTrue(exception.getMessage().contains("Token must not be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception when token is empty")
        void shouldThrowExceptionWhenTokenIsEmpty() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> jwtTokenProvider.invalidateToken(""));

            assertTrue(exception.getMessage().contains("Token must not be null or empty"));
        }

        @Test
        @DisplayName("Should throw exception when blacklisting fails")
        void shouldThrowExceptionWhenBlacklistingFails() {
            doThrow(new JwtException("Blacklisting failed")).when(tokenBlacklist)
                    .blacklist(any(), anyLong());

            assertThrows(AuthenticationException.class, () -> jwtTokenProvider.invalidateToken(validToken));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle JwtException in token validation")
        void shouldHandleJwtExceptionInTokenValidation() {
            when(keyProvider.getKey()).thenThrow(new JwtException("Test exception"));

            boolean result = jwtTokenProvider.validateToken("invalid-token");
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle JwtException in token creation")
        void shouldHandleJwtExceptionInTokenCreation() {
            when(keyProvider.getKey()).thenThrow(new JwtException("Test exception"));

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", testUserId.toString());

            assertThrows(AuthenticationException.class,
                    () -> jwtTokenProvider.generateAccessToken(testUsername, claims));
        }
    }
}