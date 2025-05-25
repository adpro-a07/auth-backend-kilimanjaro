package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenGenerator;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenParser;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenValidator;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class JwtTokenServiceImplTest {

    private JwtTokenParser jwtTokenParser;
    private JwtTokenValidator jwtTokenValidator;
    private JwtTokenGenerator jwtTokenGenerator;
    private UserRepository userRepository;
    private JwtTokenServiceImpl jwtTokenService;

    private final String email = "user@example.com";
    private final String token = "valid.jwt.token";
    private final UUID userId = UUID.randomUUID();
    private final BaseUser user = mock(BaseUser.class);

    @BeforeEach
    void setUp() {
        jwtTokenParser = mock(JwtTokenParser.class);
        jwtTokenValidator = mock(JwtTokenValidator.class);
        jwtTokenGenerator = mock(JwtTokenGenerator.class);
        userRepository = mock(UserRepository.class);
        jwtTokenService = new JwtTokenServiceImpl(
                jwtTokenParser,
                jwtTokenValidator,
                jwtTokenGenerator,
                userRepository
        );

        when(user.getId()).thenReturn(userId);
        when(user.getFullName()).thenReturn("User Name");
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user.getEmail()).thenReturn(email);
    }

    // validateToken

    @Test
    void testValidateTokenReturnsTrueIfValidAndAllClaimsPresent() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(userId.toString());
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void testValidateTokenReturnsFalseIfProviderSaysInvalid() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(false);

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateTokenReturnsFalseIfRoleClaimMissing() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn(null);
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(userId.toString());

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateTokenReturnsFalseIfFullNameClaimMissing() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn(null);
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(userId.toString());

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateTokenReturnsFalseIfUserIdClaimMissing() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(null);

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateTokenReturnsFalseIfExceptionThrown() {
        when(jwtTokenValidator.validateToken(token)).thenThrow(new RuntimeException("Boom"));

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateToken_withMatchingTokenType_shouldReturnTrue() {
        String token = "validToken";
        String tokenType = "access";

        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("John Doe");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn("1234");
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");

        boolean result = jwtTokenService.validateToken(token, tokenType);
        assertTrue(result);
    }

    @Test
    void testValidateToken_withMismatchedTokenType_shouldReturnFalse() {
        String token = "validToken";
        String tokenType = "refresh";

        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("John Doe");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn("1234");
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");

        boolean result = jwtTokenService.validateToken(token, tokenType);
        assertFalse(result);
    }

    @Test
    void testValidateToken_withNullTokenType_shouldReturnTrue() {
        String token = "validToken";

        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("John Doe");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn("1234");
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");

        boolean result = jwtTokenService.validateToken(token, null);
        assertTrue(result);
    }

    @Test
    void testValidateToken_withMissingClaims_shouldReturnFalse() {
        String token = "tokenWithMissingClaims";
        String tokenType = "access";

        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn(null); // Missing role
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("John Doe");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn("1234");
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");

        boolean result = jwtTokenService.validateToken(token, tokenType);
        assertFalse(result);
    }

    @Test
    void testValidateToken_withInvalidToken_shouldReturnFalse() {
        String token = "invalidToken";
        String tokenType = "access";

        when(jwtTokenValidator.validateToken(token)).thenReturn(false);

        boolean result = jwtTokenService.validateToken(token, tokenType);
        assertFalse(result);
    }

    @Test
    void testValidateToken_whenExceptionThrown_shouldReturnFalse() {
        String token = "exceptionToken";
        String tokenType = "access";

        when(jwtTokenValidator.validateToken(token)).thenThrow(new RuntimeException("Boom"));

        boolean result = jwtTokenService.validateToken(token, tokenType);
        assertFalse(result);
    }

    // refreshToken

    @Test
    void testRefreshTokenReturnsNewTokensIfValid() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(userId.toString());
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");
        when(jwtTokenParser.getEmailFromToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenGenerator.generateAccessToken(eq(email), anyMap())).thenReturn("new-access");
        when(jwtTokenGenerator.generateRefreshToken(eq(email), anyMap())).thenReturn("new-refresh");

        JwtTokenService.TokenPair tokens = jwtTokenService.refreshToken(token);

        assertThat(tokens.accessToken()).isEqualTo("new-access");
        assertThat(tokens.refreshToken()).isEqualTo("new-refresh");

        // Make sure refresh token is now invalid
        verify(jwtTokenValidator).invalidateToken(token);
    }

    @Test
    void testRefreshTokenThrowsIfTokenInvalid() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> jwtTokenService.refreshToken(token))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid refresh token");

        verify(jwtTokenValidator, never()).invalidateToken(token);
    }

    // getUserFromToken

    @Test
    void testGetUserFromTokenReturnsUserIfValid() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(userId.toString());
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");
        when(jwtTokenParser.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BaseUser result = jwtTokenService.getUserFromToken(token);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void testGetUserFromTokenThrowsIfInvalidToken() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> jwtTokenService.getUserFromToken(token))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testGetUserFromTokenThrowsIfUserNotFound() {
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        when(jwtTokenParser.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenParser.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenParser.getClaimFromToken(token, "userId", String.class)).thenReturn(userId.toString());
        when(jwtTokenParser.getTokenType(token)).thenReturn("access");
        when(jwtTokenParser.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtTokenService.getUserFromToken(token))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User not found in token");
    }

    // invalidateToken

    @Test
    void testInvalidateTokenCallsProvider() {
        jwtTokenService.invalidateToken(token);

        verify(jwtTokenValidator).invalidateToken(token);
    }

    @Test
    void testInvalidateTokenHandlesExceptionGracefully() {
        doThrow(new RuntimeException("Fail")).when(jwtTokenValidator).invalidateToken(token);

        jwtTokenService.invalidateToken(token); // should not throw
    }

    // generateTokensFromEmail

    @Test
    void testGenerateTokensFromEmailReturnsTokens() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenGenerator.generateAccessToken(eq(email), anyMap())).thenReturn("access");
        when(jwtTokenGenerator.generateRefreshToken(eq(email), anyMap())).thenReturn("refresh");

        JwtTokenService.TokenPair pair = jwtTokenService.generateTokensFromEmail(email);

        assertThat(pair.accessToken()).isEqualTo("access");
        assertThat(pair.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void testGenerateTokensFromEmailThrowsIfUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtTokenService.generateTokensFromEmail(email))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email");
    }
}

