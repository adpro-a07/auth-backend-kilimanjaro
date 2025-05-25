package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryTokenBlacklist(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void blacklist(String token, long expiryEpochMillis) {
        blacklist.put(token, expiryEpochMillis);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Long expiry = blacklist.get(token);
        if (expiry == null) return false;

        long now = clock.millis(); // Use injected clock
        if (expiry < now) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }
}
