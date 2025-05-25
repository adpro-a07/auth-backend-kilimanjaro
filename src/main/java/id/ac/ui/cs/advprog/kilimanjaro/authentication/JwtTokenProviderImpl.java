package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenGenerator;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenParser;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenValidator;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtTokenProviderImpl implements JwtTokenGenerator, JwtTokenParser, JwtTokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderImpl.class);

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String USER_ID_CLAIM = "userId";

    private final SigningKeyProvider keyProvider;
    private final UserRepository userRepository;
    private final JwtProperties properties;
    private final TokenBlacklist blacklist;
    private final Clock clock;

    public JwtTokenProviderImpl(SigningKeyProvider keyProvider,
                                UserRepository userRepository,
                                JwtProperties properties,
                                TokenBlacklist blacklist,
                                Clock clock) {
        this.keyProvider = Objects.requireNonNull(keyProvider, "SigningKeyProvider must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null");
        this.properties = Objects.requireNonNull(properties, "JwtProperties must not be null");
        this.blacklist = Objects.requireNonNull(blacklist, "TokenBlacklist must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public String generateAccessToken(String username, Map<String, Object> additionalClaims) {
        validateTokenInputs(username, additionalClaims);

        Map<String, Object> claims = new HashMap<>(additionalClaims);
        claims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS);

        return createToken(username, claims, properties.getAccessExpiration());
    }

    @Override
    public String generateRefreshToken(String username, Map<String, Object> additionalClaims) {
        validateTokenInputs(username, additionalClaims);

        Map<String, Object> claims = new HashMap<>(additionalClaims);
        claims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH);

        return createToken(username, claims, properties.getRefreshExpiration());
    }

    private void validateTokenInputs(String username, Map<String, Object> additionalClaims) {
        Assert.hasText(username, "Username must not be null or empty");
        Assert.notNull(additionalClaims, "Additional claims must not be null");
    }

    private String createToken(String subject, Map<String, Object> claims, long expiration) {
        Instant now = clock.instant();
        Instant expiry = now.plusMillis(expiration);

        try {
            return Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiry))
                    .addClaims(claims)
                    .signWith(keyProvider.getKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (JwtException e) {
            logger.error("Error creating JWT token", e);
            throw new AuthenticationException("Failed to create token", e);
        }
    }

    @Override
    public String getEmailFromToken(String token) {
        validateToken(token);
        return getAllClaimsFromToken(token).getSubject();
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        validateToken(token);
        try {
            String userId = getAllClaimsFromToken(token).get(USER_ID_CLAIM, String.class);
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user ID in token", e);
            throw new AuthenticationException("Invalid user ID format in token", e);
        }
    }

    @Override
    public String getTokenType(String token) {
        validateToken(token);
        return getAllClaimsFromToken(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    @Override
    public <T> T getClaimFromToken(String token, String claimName, Class<T> claimType) {
        validateToken(token);
        return getAllClaimsFromToken(token).get(claimName, claimType);
    }

    @Override
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        validateToken(token);
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(keyProvider.getKey())
                    .setClock(() -> Date.from(clock.instant()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Failed to parse JWT claims", e);
            throw new AuthenticationException("Invalid token", e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            if (blacklist.isBlacklisted(token)) {
                logger.warn("Token is blacklisted");
                return false;
            }

            Claims claims = getAllClaimsFromToken(token);

            if (isTokenExpired(claims)) {
                logger.warn("Token is expired");
                return false;
            }

            String userId = claims.get(USER_ID_CLAIM, String.class);
            return verifyUserExists(userId);

        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid token", e);
            return false;  // Returning false for any JwtException
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            return false;  // Handle unexpected errors gracefully
        }
    }

    private boolean verifyUserExists(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            return userRepository.existsById(userUuid);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid user ID format in token", e);
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(Date.from(clock.instant()));
    }

    @Override
    public void invalidateToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            Instant expiry = getAllClaimsFromToken(token).getExpiration().toInstant();
            blacklist.blacklist(token, expiry.toEpochMilli());
            logger.debug("Token invalidated and blacklisted");
        } catch (JwtException e) {
            logger.error("Failed to invalidate token", e);
            throw new AuthenticationException("Failed to invalidate token", e);
        }
    }

    @Override
    public Date getExpirationDateFromToken(String token) {
        validateToken(token);
        return getClaimFromToken(token, Claims::getExpiration);
    }
}
