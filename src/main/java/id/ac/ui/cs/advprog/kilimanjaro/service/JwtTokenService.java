package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;

public interface JwtTokenService {

    /**
     * Record to represent a pair of access and refresh tokens
     */
    record TokenPair(String accessToken, String refreshToken) {}

    /**
     * Validates if a token is valid and not expired
     *
     * @param token The JWT token to validate
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Validates if a token is valid and not expired and is of the same type
     *
     * @param token The JWT token to validate
     * @param tokenType The type of token to validate
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token, String tokenType);

    /**
     * Refreshes an access token using a refresh token
     *
     * @param refreshToken The refresh token
     * @return A new pair of access and refresh tokens
     * @throws AuthenticationException if the refresh token is invalid or expired
     */
    TokenPair refreshToken(String refreshToken) throws AuthenticationException;

    /**
     * Extracts the user ID from a JWT token and returns the user object
     *
     * @param token The JWT token
     * @return The user
     * @throws AuthenticationException if token is invalid or cannot extract user ID
     */
    BaseUser getUserFromToken(String token) throws AuthenticationException;

    /**
     * Invalidates a token (for logout)
     *
     * @param token The JWT token to invalidate
     */
    void invalidateToken(String token);

    /**
     * Generates a new access and refresh token pair for a given email
     *
     * @param email The email of the user
     * @return A new pair of access and refresh tokens
     */
    TokenPair generateTokensFromEmail(String email);
}
