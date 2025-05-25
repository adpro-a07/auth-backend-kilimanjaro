package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.Key;
import java.util.Objects;

/**
 * Implementation of SigningKeyProvider that creates and provides
 * HMAC-SHA signing keys from Base64 encoded secrets
 */
@Component
public class SigningKeyProviderImpl implements SigningKeyProvider {
    private final Key key;

    /**
     * Creates a new SigningKeyProviderImpl with the specified properties
     *
     * @param properties the JWT properties containing the secret
     * @throws NullPointerException if properties or the secret is null
     * @throws IllegalArgumentException if the secret is empty, not valid Base64,
     *         or not long enough for HMAC-SHA algorithm
     */
    public SigningKeyProviderImpl(JwtProperties properties) {
        Objects.requireNonNull(properties, "JwtProperties must not be null");
        String secret = properties.getSecret();
        Objects.requireNonNull(secret, "JWT secret must not be null");
        Assert.hasText(secret, "JWT secret must not be empty");

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            // Keys.hmacShaKeyFor will validate the key length
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JWT secret: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the signing key
     *
     * @return the signing key
     */
    @Override
    public Key getKey() {
        return key;
    }
}