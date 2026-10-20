package com.ahmadamirizal.leave.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing: after {@value #MAX_FAILURES} wrong passwords for a username, that username is locked
 * for {@link #LOCK} even with the right password. Kept in memory, which is enough for a single server.
 */
public class LoginThrottle {

    public static final int MAX_FAILURES = 5;
    public static final Duration LOCK = Duration.ofMinutes(5);

    private record Failures(int count, Instant last) {}

    private final Map<String, Failures> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    public boolean isLocked(String username) {
        Failures f = failures.get(key(username));
        if (f == null) return false;
        if (f.last().plus(LOCK).isBefore(clock.instant())) {
            failures.remove(key(username));
            return false;
        }
        return f.count() >= MAX_FAILURES;
    }

    public void failed(String username) {
        failures.merge(key(username), new Failures(1, clock.instant()),
                (old, x) -> new Failures(old.count() + 1, clock.instant()));
    }

    public void succeeded(String username) {
        failures.remove(key(username));
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
