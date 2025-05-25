package id.ac.ui.cs.advprog.kilimanjaro.authentication;

public interface TokenBlacklist {
    void blacklist(String token, long expiryEpochMillis);
    boolean isBlacklisted(String token);
}
