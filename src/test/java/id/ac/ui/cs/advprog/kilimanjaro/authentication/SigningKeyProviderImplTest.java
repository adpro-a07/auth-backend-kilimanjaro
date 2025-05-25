package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SigningKeyProviderImplTest {

    @Mock
    private JwtProperties properties;

    @Test
    void constructor_ShouldInitializeKeyCorrectly_WhenGivenValidSecret() {
        // Given
        when(properties.getSecret()).thenReturn("c2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaEZvclNpZ25pbmdKd3RUb2tlbnNJblRoZUFwcGxpY2F0aW9u");

        // When
        SigningKeyProviderImpl provider = new SigningKeyProviderImpl(properties);

        // Then
        assertNotNull(provider.getKey());

        // Verify the key works by creating and validating a JWT
        String token = createToken(provider.getKey());
        assertDoesNotThrow(() -> validateToken(token, provider.getKey()));
    }

    @Test
    void constructor_ShouldThrowException_WhenGivenInvalidBase64Secret() {
        // Given
        when(properties.getSecret()).thenReturn("not-a-valid-base64-string");

        // When & Then
        assertThrows(DecodingException.class, () -> new SigningKeyProviderImpl(properties));
    }

    @Test
    void constructor_ShouldThrowException_WhenGivenNullSecret() {
        // Given
        when(properties.getSecret()).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, () -> new SigningKeyProviderImpl(properties));
    }

    @Test
    void constructor_ShouldThrowException_WhenGivenEmptySecret() {
        // Given
        when(properties.getSecret()).thenReturn("");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new SigningKeyProviderImpl(properties));
    }

    @Test
    void constructor_ShouldThrowException_WhenSecretIsTooShort() {
        // Given - short secret that will be rejected by HMAC-SHA algorithm
        when(properties.getSecret()).thenReturn("dG9vU2hvcnQ=");  // "tooShort" in Base64

        // When & Then
        assertThrows(WeakKeyException.class, () -> new SigningKeyProviderImpl(properties));
    }

    @Test
    void getKey_ShouldReturnSameKeyInstance_WhenCalledMultipleTimes() {
        // Given
        when(properties.getSecret()).thenReturn("c2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaEZvclNpZ25pbmdKd3RUb2tlbnNJblRoZUFwcGxpY2F0aW9u");
        SigningKeyProviderImpl provider = new SigningKeyProviderImpl(properties);

        // When
        Key key1 = provider.getKey();
        Key key2 = provider.getKey();

        // Then
        assertSame(key1, key2, "getKey() should return the same key instance on multiple calls");
    }

    @Test
    void keyGeneration_ShouldBeConsistent_WithSameSecret() {
        // Given
        String secret = "c2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaEZvclNpZ25pbmdKd3RUb2tlbnNJblRoZUFwcGxpY2F0aW9u";
        when(properties.getSecret()).thenReturn(secret);

        // When
        SigningKeyProviderImpl provider1 = new SigningKeyProviderImpl(properties);
        SigningKeyProviderImpl provider2 = new SigningKeyProviderImpl(properties);

        // Then
        String token = createToken(provider1.getKey());
        assertDoesNotThrow(() -> validateToken(token, provider2.getKey()));
    }

    @Test
    void key_ShouldFailValidation_WithDifferentSecret() {
        // Given
        String secret1 = "c2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaEZvclNpZ25pbmdKd3RUb2tlbnNJblRoZUFwcGxpY2F0aW9u";
        String secret2 = "YW5vdGhlclNlY3JldEtleVRoYXRJc0RpZmZlcmVudEJ1dEFsc29Mb25nRW5vdWdoRm9ySG1hY1NoYTI1Ng==";

        when(properties.getSecret()).thenReturn(secret1);
        SigningKeyProviderImpl provider1 = new SigningKeyProviderImpl(properties);

        when(properties.getSecret()).thenReturn(secret2);
        SigningKeyProviderImpl provider2 = new SigningKeyProviderImpl(properties);

        // When
        String token = createToken(provider1.getKey());

        // Then
        assertThrows(JwtException.class, () -> validateToken(token, provider2.getKey()));
    }

    /**
     * Helper method to create a token with the given key
     */
    private String createToken(Key key) {
        return Jwts.builder()
                .setSubject("test")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Helper method to validate a token with the given key
     */
    private void validateToken(String token, Key key) {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}