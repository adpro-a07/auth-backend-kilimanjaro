package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT authentication.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    // Remove secret key for HS256
    // private String secret;

    // Add private and public keys for RS256
    private String privateKey;  // Could be base64 or PEM encoded
    private String publicKey;   // Could be base64 or PEM encoded

    private long accessExpiration;
    private long refreshExpiration;
}
