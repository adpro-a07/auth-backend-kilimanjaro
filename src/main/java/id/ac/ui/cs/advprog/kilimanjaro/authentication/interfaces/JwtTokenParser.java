package id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

public interface JwtTokenParser {
    String getEmailFromToken(String token);
    UUID getUserIdFromToken(String token);
    String getTokenType(String token);
    Date getExpirationDateFromToken(String token);
    <T> T getClaimFromToken(String token, String claimName, Class<T> claimType);
    <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver);
}
