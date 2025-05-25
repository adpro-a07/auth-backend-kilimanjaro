package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenGenerator;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenParser;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces.JwtTokenValidator;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_FULL_NAME = "fullName";
    private static final String CLAIM_USER_ID = "userId";

    private final JwtTokenParser jwtTokenParser;
    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final UserRepository userRepository;

    public JwtTokenServiceImpl(
            JwtTokenParser jwtTokenParser,
            JwtTokenValidator jwtTokenValidator,
            JwtTokenGenerator jwtTokenGenerator,
            UserRepository userRepository
    ) {
        this.jwtTokenParser = jwtTokenParser;
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.userRepository = userRepository;
    }

    @Override
    public boolean validateToken(String token) {
        return validateToken(token, null);
    }

    @Override
    public boolean validateToken(String token, String tokenType) {
        try {
            if (!jwtTokenValidator.validateToken(token)) {
                return false;
            }

            String role = jwtTokenParser.getClaimFromToken(token, CLAIM_ROLE, String.class);
            String fullName = jwtTokenParser.getClaimFromToken(token, CLAIM_FULL_NAME, String.class);
            String userId = jwtTokenParser.getClaimFromToken(token, CLAIM_USER_ID, String.class);
            String claimTokenType = jwtTokenParser.getTokenType(token);

            if (role == null || fullName == null || userId == null || claimTokenType == null) {
                logger.warn("Token claims are missing or invalid");
                return false;
            }

            if (tokenType == null) {
                return true;
            }

            return tokenType.equals(claimTokenType);
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }


    @Override
    public TokenPair refreshToken(String refreshToken) throws AuthenticationException {
        if (!validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String email = jwtTokenParser.getEmailFromToken(refreshToken);
        jwtTokenValidator.invalidateToken(refreshToken);

        return generateTokensFromEmail(email);
    }

    @Override
    public BaseUser getUserFromToken(String token) throws AuthenticationException {
        if (!validateToken(token)) {
            throw new AuthenticationException("Invalid token");
        }

        UUID userId = jwtTokenParser.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found in token"));
    }

    @Override
    @Async
    public void invalidateToken(String token) {
        try {
            jwtTokenValidator.invalidateToken(token);
        } catch (Exception e) {
            logger.error("Failed to invalidate token: {}", e.getMessage());
        }
    }

    @Override
    public TokenPair generateTokensFromEmail(String email) {
        BaseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROLE, user.getRole());
        claims.put(CLAIM_FULL_NAME, user.getFullName());
        claims.put(CLAIM_USER_ID, user.getId());

        String accessToken = jwtTokenGenerator.generateAccessToken(email, claims);
        String refreshToken = jwtTokenGenerator.generateRefreshToken(email, claims);

        return new TokenPair(accessToken, refreshToken);
    }
}
