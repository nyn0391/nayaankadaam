package com.busgo.auth;

import com.busgo.auth.dto.AuthRequest;
import com.busgo.auth.dto.AuthResponse;
import com.busgo.auth.dto.RegisterRequest;
import com.busgo.auth.dto.RefreshRequest;
import com.busgo.token.RefreshToken;
import com.busgo.token.RefreshTokenService;
import com.busgo.user.User;
import com.busgo.user.UserRepository;
import com.busgo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.expiration_ms:3600000}")
    private long accessTtlMs;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and password required"));
        }

        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("success", false, "message", "Email already registered"));
        }

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setMobile(req.getMobile());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepo.save(u);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
        }

        Optional<User> ou = userRepo.findByEmail(req.getUsername());
        if (ou.isEmpty()) ou = userRepo.findByMobile(req.getUsername());
        if (ou.isEmpty()) return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        User user = ou.get();

        String access = jwtUtil.generateToken(user.getUsername(), Map.of("uid", user.getId().toString()), accessTtlMs);
        RefreshToken rt = refreshTokenService.createToken(user.getId());

        AuthResponse resp = new AuthResponse(access, rt.getToken());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        String r = req.getRefreshToken();
        if (r == null) return ResponseEntity.badRequest().build();
        Optional<RefreshToken> ot = refreshTokenService.findByToken(r);
        if (ot.isEmpty()) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid refresh token"));
        RefreshToken token = ot.get();
        if (token.getExpiry().isBefore(OffsetDateTime.now())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Refresh token expired"));
        }

        Optional<User> ou = userRepo.findById(token.getUserId());
        if (ou.isEmpty()) return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        User user = ou.get();

        String access = jwtUtil.generateToken(user.getUsername(), Map.of("uid", user.getId().toString()), accessTtlMs);
        AuthResponse resp = new AuthResponse(access, token.getToken());
        return ResponseEntity.ok(resp);
    }
}
