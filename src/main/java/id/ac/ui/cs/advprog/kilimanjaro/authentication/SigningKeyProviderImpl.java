package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * Implementation of SigningKeyProvider that creates and provides
 * RSA private signing keys for RS256 from Base64 or PEM encoded private key strings.
 */
@Component
public class SigningKeyProviderImpl implements SigningKeyProvider {
    private final PrivateKey privateKey;

    /**
     * Creates a new SigningKeyProviderImpl with the specified properties
     *
     * @param properties the JWT properties containing the private key string
     * @throws NullPointerException if properties or the privateKey is null
     * @throws IllegalArgumentException if the private key is invalid or cannot be parsed
     */
    public SigningKeyProviderImpl(JwtProperties properties) {
        Objects.requireNonNull(properties, "JwtProperties must not be null");
        String privateKeyStr = properties.getPrivateKey();
        Objects.requireNonNull(privateKeyStr, "JWT privateKey must not be null");
        Assert.hasText(privateKeyStr, "JWT privateKey must not be empty");

        try {
            this.privateKey = loadPrivateKey(privateKeyStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT private key: " + e.getMessage(), e);
        }
    }

    private PrivateKey loadPrivateKey(String base64Pem) throws Exception {
        // Decode full PEM file from base64
        String pem = new String(Base64.getDecoder().decode(base64Pem));

        String privateKeyPEM;
        if (pem.contains("-----BEGIN RSA PRIVATE KEY-----")) {
            // PKCS#1 format
            privateKeyPEM = pem
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            // Convert PKCS#1 to PKCS#8
            byte[] pkcs1Bytes = Base64.getDecoder().decode(privateKeyPEM);
            byte[] pkcs8Bytes = convertPkcs1ToPkcs8(pkcs1Bytes);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);

        } else if (pem.contains("-----BEGIN PRIVATE KEY-----")) {
            // PKCS#8 format (your original code)
            privateKeyPEM = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } else {
            throw new IllegalArgumentException("Unsupported private key format");
        }
    }

    private byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) {
        // PKCS#8 wrapper for RSA private key
        byte[] pkcs8Header = {
                0x30, (byte) 0x82, 0x00, 0x00, // SEQUENCE, length placeholder
                0x02, 0x01, 0x00, // INTEGER version = 0
                0x30, 0x0d, // SEQUENCE (AlgorithmIdentifier)
                0x06, 0x09, 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, // OID rsaEncryption
                0x05, 0x00, // NULL parameters
                0x04, (byte) 0x82, 0x00, 0x00 // OCTET STRING, length placeholder
        };

        int totalLength = pkcs8Header.length + pkcs1Bytes.length;
        int sequenceLength = totalLength - 4; // Subtract the initial SEQUENCE tag and length
        int octetStringLength = pkcs1Bytes.length;

        // Update length fields
        pkcs8Header[2] = (byte) ((sequenceLength >> 8) & 0xff);
        pkcs8Header[3] = (byte) (sequenceLength & 0xff);
        pkcs8Header[pkcs8Header.length - 2] = (byte) ((octetStringLength >> 8) & 0xff);
        pkcs8Header[pkcs8Header.length - 1] = (byte) (octetStringLength & 0xff);

        // Combine header and PKCS#1 data
        byte[] pkcs8Bytes = new byte[totalLength];
        System.arraycopy(pkcs8Header, 0, pkcs8Bytes, 0, pkcs8Header.length);
        System.arraycopy(pkcs1Bytes, 0, pkcs8Bytes, pkcs8Header.length, pkcs1Bytes.length);

        return pkcs8Bytes;
    }

    /**
     * Returns the private signing key
     *
     * @return the private signing key
     */
    @Override
    public Key getPrivateKey() {
        return privateKey;
    }
}
