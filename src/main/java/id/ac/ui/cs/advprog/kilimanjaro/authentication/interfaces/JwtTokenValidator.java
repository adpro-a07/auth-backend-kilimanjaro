package id.ac.ui.cs.advprog.kilimanjaro.authentication.interfaces;

public interface JwtTokenValidator {
    boolean validateToken(String token);
    void invalidateToken(String token);
}
