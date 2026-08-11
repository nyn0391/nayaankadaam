package com.busgo.booking;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SeatLockService {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<String> lockSeatsScript;
    private final DefaultRedisScript<String> confirmHoldScript;
    private final Duration lockTtl = Duration.ofMinutes(5);

    public SeatLockService(StringRedisTemplate redis,
                           DefaultRedisScript<String> lockSeatsScript,
                           DefaultRedisScript<String> confirmHoldScript) {
        this.redis = redis;
        this.lockSeatsScript = lockSeatsScript;
        this.confirmHoldScript = confirmHoldScript;
    }

    private String seatKey(String tripId, String seatCode) {
        return "seatlock:" + tripId + ":" + seatCode;
    }

    public record HoldResult(String holdToken, List<String> conflicts) {}

    /**
     * Attempt to lock seats atomically using Lua script. Returns HoldResult with a holdToken when successful.
     * If conflicts exist, holdToken will be null and conflicts contain the conflicting seat keys.
     */
    public HoldResult holdSeats(String tripId, List<String> seatCodes, String userId) {
        if (seatCodes == null || seatCodes.isEmpty()) return new HoldResult(null, List.of());
        List<String> keys = new ArrayList<>();
        for (String s : seatCodes) keys.add(seatKey(tripId, s));
        String holdToken = UUID.randomUUID().toString();
        String mapKey = "hold:" + holdToken;
        // args: holdToken, userId, ttlSeconds, mapKey
        Long ttlSeconds = lockTtl.getSeconds();
        try {
            Object res = redis.execute(lockSeatsScript, keys, holdToken, userId, ttlSeconds.toString(), mapKey);
            if (res != null && res instanceof String) {
                String r = (String) res;
                if (r.equals("OK")) {
                    return new HoldResult(holdToken, List.of());
                }
                if (r.startsWith("CONFLICT:")) {
                    String conflictKey = r.substring("CONFLICT:".length());
                    return new HoldResult(null, List.of(conflictKey));
                }
            }
        } catch (Exception ex) {
            // fallback: indicate conflict
            return new HoldResult(null, List.of("error"));
        }
        return new HoldResult(null, List.of("unknown"));
    }

    public boolean confirmHold(String holdToken) {
        String mapKey = "hold:" + holdToken;
        try {
            Object res = redis.execute(confirmHoldScript, List.of(mapKey), holdToken);
            if (res != null && res instanceof String) {
                String r = (String) res;
                return r.equals("OK");
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public void releaseHold(String holdToken) {
        String mapKey = "hold:" + holdToken;
        List<String> seats = redis.opsForList().range(mapKey, 0, -1);
        if (seats != null) {
            for (String k : seats) redis.delete(k);
        }
        redis.delete(mapKey);
    }
}
