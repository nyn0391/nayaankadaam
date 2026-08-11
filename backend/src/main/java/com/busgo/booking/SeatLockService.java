package com.busgo.booking;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeatLockService {

    private final RedisTemplate<String, String> redis;
    private final Duration lockTtl = Duration.ofMinutes(5);

    public SeatLockService(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    private String key(String tripId, String seatCode) {
        return "seatlock:" + tripId + ":" + seatCode;
    }

    /**
     * Attempt to lock seats for a given trip. Returns a hold token if successful (random UUID).
     * If any seat is already locked, returns null.
     */
    public String holdSeats(String tripId, List<String> seatCodes, String userId) {
        // simple optimistic approach: check and set
        List<String> keys = seatCodes.stream().map(s -> key(tripId, s)).collect(Collectors.toList());
        // check
        for (String k : keys) {
            Boolean exists = redis.hasKey(k);
            if (Boolean.TRUE.equals(exists)) return null;
        }
        String holdToken = UUID.randomUUID().toString();
        for (String s : seatCodes) {
            String k = key(tripId, s);
            redis.opsForValue().set(k, holdToken + ":" + userId, lockTtl);
        }
        // store a mapping from holdToken to seats for easy confirmation later
        String mapKey = "hold:" + holdToken;
        redis.opsForList().rightPushAll(mapKey, keys.toArray(new String[0]));
        redis.expire(mapKey, lockTtl);
        return holdToken;
    }

    public boolean confirmHold(String holdToken) {
        String mapKey = "hold:" + holdToken;
        Long size = redis.opsForList().size(mapKey);
        if (size == null || size == 0) return false;
        // free keys and keep as booked marker; in a real system we'd mark DB booked and remove locks
        List<String> keys = redis.opsForList().range(mapKey, 0, -1);
        if (keys == null) return false;
        for (String k : keys) {
            redis.delete(k);
        }
        redis.delete(mapKey);
        return true;
    }

    public void releaseHold(String holdToken) {
        String mapKey = "hold:" + holdToken;
        List<String> keys = redis.opsForList().range(mapKey, 0, -1);
        if (keys != null) {
            for (String k : keys) redis.delete(k);
        }
        redis.delete(mapKey);
    }
}
