package id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces;

import java.util.Map;

public interface JwtTokenGenerator {
    String generateAccessToken(String username, Map<String, Object> additionalClaims);
    String generateRefreshToken(String username, Map<String, Object> additionalClaims);
}
