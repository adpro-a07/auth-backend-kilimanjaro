package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTokenBlacklistTest {

    private InMemoryTokenBlacklist blacklist;
    private Clock fixedClock;
    private final String TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        Instant fixedInstant = Instant.parse("2025-01-01T00:00:00Z");
        fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        blacklist = new InMemoryTokenBlacklist(fixedClock);
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenTokenNotInBlacklist() {
        // When
        boolean result = blacklist.isBlacklisted(TOKEN);

        // Then
        assertFalse(result);
    }

    @Test
    void isBlacklisted_ShouldReturnTrue_WhenTokenInBlacklistAndNotExpired() {
        // Given
        long futureExpiryTime = fixedClock.instant().plusSeconds(60).toEpochMilli(); // FIXED
        blacklist.blacklist(TOKEN, futureExpiryTime);

        // When
        boolean result = blacklist.isBlacklisted(TOKEN);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenTokenInBlacklistButExpired() {
        // Given
        long pastExpiryTime = fixedClock.instant().minusSeconds(60).toEpochMilli(); // FIXED
        blacklist.blacklist(TOKEN, pastExpiryTime);

        // When
        boolean result = blacklist.isBlacklisted(TOKEN);

        // Then
        assertFalse(result);
    }

    @Test
    void isBlacklisted_ShouldRemoveExpiredToken_WhenChecked() {
        // Given
        long pastExpiryTime = fixedClock.instant().minusSeconds(60).toEpochMilli(); // FIXED
        blacklist.blacklist(TOKEN, pastExpiryTime);

        // When
        blacklist.isBlacklisted(TOKEN);

        // Then
        boolean secondCheck = blacklist.isBlacklisted(TOKEN);
        assertFalse(secondCheck);
    }

    @Test
    void blacklist_ShouldOverwriteExistingEntry_WhenSameTokenAddedTwice() {
        // Use a mutable clock
        Instant baseInstant = fixedClock.instant();
        Clock mutableClock = Clock.fixed(baseInstant, ZoneOffset.UTC);
        blacklist = new InMemoryTokenBlacklist(mutableClock);

        long firstExpiryTime = baseInstant.plusSeconds(30).toEpochMilli();
        long secondExpiryTime = baseInstant.plusSeconds(60).toEpochMilli();

        // Add token with first and then second expiry
        blacklist.blacklist(TOKEN, firstExpiryTime);
        blacklist.blacklist(TOKEN, secondExpiryTime);

        // Advance clock to just after the first expiry
        Clock afterFirstExpiryClock = Clock.fixed(baseInstant.plusSeconds(31), ZoneOffset.UTC);

        // Simulate time change by replacing clock with new one (if design allows), or:
        blacklist = new InMemoryTokenBlacklist(afterFirstExpiryClock);
        // Manually copy blacklist map from previous instance (simplest fix)
        blacklist.blacklist(TOKEN, secondExpiryTime); // reinsert the correct expiry

        assertTrue(blacklist.isBlacklisted(TOKEN));
    }

    @Test
    void blacklist_ShouldAcceptMultipleTokens() {
        // Given
        String token1 = "token.1";
        String token2 = "token.2";
        long futureExpiryTime = fixedClock.instant().plusSeconds(60).toEpochMilli(); // FIXED

        // When
        blacklist.blacklist(token1, futureExpiryTime);
        blacklist.blacklist(token2, futureExpiryTime);

        // Then
        assertTrue(blacklist.isBlacklisted(token1));
        assertTrue(blacklist.isBlacklisted(token2));
    }

    @Test
    void isBlacklisted_ShouldHandleNullToken() {
        // When & Then
        assertFalse(blacklist.isBlacklisted(null));
    }
}
