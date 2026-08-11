package com.busgo.trip;

import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.user.User;
import com.busgo.user.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trips")
public class TripSeatController {

    private final TripSeatRepository tripSeatRepository;
    private final StringRedisTemplate redis;
    private final UserRepository userRepository;

    public TripSeatController(TripSeatRepository tripSeatRepository, StringRedisTemplate redis, UserRepository userRepository) {
        this.tripSeatRepository = tripSeatRepository;
        this.redis = redis;
        this.userRepository = userRepository;
    }

    private String resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
            Object uid = jwt.getClaim("uid");
            String uidStr = uid != null ? uid.toString() : jwt.getSubject();
            return uidStr;
        }
        String principal = auth.getName();
        if (principal != null) {
            User user = userRepository.findByEmail(principal).orElseGet(() -> userRepository.findByMobile(principal).orElse(null));
            if (user != null) return user.getId().toString();
        }
        return null;
    }

    @GetMapping("/{tripId}/seats")
    public ResponseEntity<?> getSeats(@PathVariable String tripId) {
        UUID tid;
        try { tid = UUID.fromString(tripId); } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("success", false, "message", "invalid tripId")); }

        String currentUserId = resolveCurrentUserId();

        List<TripSeat> tripSeats = tripSeatRepository.findByTripId(tid);
        List<Map<String, Object>> seats = tripSeats.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("seatCode", s.getSeatCode());
            m.put("isBooked", s.getIsBooked());
            m.put("price", s.getPrice());
            String key = "seatlock:" + tripId + ":" + s.getSeatCode();
            String val = redis.opsForValue().get(key);
            if (val != null) {
                // val format: holdToken:userId
                String[] parts = val.split(":", 2);
                String holder = parts.length > 1 ? parts[1] : parts[0];
                if (currentUserId != null && currentUserId.equals(holder)) {
                    m.put("heldBy", "you");
                    m.put("heldByUserId", holder);
                } else {
                    m.put("heldBy", "other");
                    m.put("heldByUserId", holder);
                }
            } else {
                m.put("heldBy", null);
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(seats);
    }
}
EOF
