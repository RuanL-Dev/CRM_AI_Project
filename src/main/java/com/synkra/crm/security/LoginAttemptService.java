package com.synkra.crm.security;

import com.synkra.crm.config.SecurityProperties;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final Map<String, AttemptState> attemptsByClient = new ConcurrentHashMap<>();
    private final SecurityProperties securityProperties;
    private final Clock clock = Clock.systemUTC();

    public LoginAttemptService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public boolean isBlocked(String clientKey) {
        if (!securityProperties.getLoginRateLimit().isEnabled()) {
            return false;
        }
        AttemptState attemptState = attemptsByClient.get(clientKey);
        if (attemptState == null) {
            return false;
        }
        Instant now = Instant.now(clock);
        if (attemptState.blockedUntil != null && attemptState.blockedUntil.isAfter(now)) {
            return true;
        }
        if (attemptState.windowStartedAt.plus(securityProperties.getLoginRateLimit().getWindow()).isBefore(now)) {
            attemptsByClient.remove(clientKey);
        }
        return false;
    }

    public void recordFailure(String clientKey) {
        if (!securityProperties.getLoginRateLimit().isEnabled()) {
            return;
        }
        Instant now = Instant.now(clock);
        attemptsByClient.compute(clientKey, (key, current) -> {
            if (current == null || current.windowStartedAt.plus(securityProperties.getLoginRateLimit().getWindow()).isBefore(now)) {
                current = new AttemptState(now, 0, null);
            }
            int failureCount = current.failureCount + 1;
            Instant blockedUntil = current.blockedUntil;
            if (failureCount >= securityProperties.getLoginRateLimit().getMaxFailures()) {
                blockedUntil = now.plus(securityProperties.getLoginRateLimit().getLockout());
            }
            return new AttemptState(current.windowStartedAt, failureCount, blockedUntil);
        });
    }

    public void recordSuccess(String clientKey) {
        if (!securityProperties.getLoginRateLimit().isEnabled()) {
            return;
        }
        attemptsByClient.remove(clientKey);
    }

    private record AttemptState(Instant windowStartedAt, int failureCount, Instant blockedUntil) {
    }
}
